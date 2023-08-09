package mills.score.opening;

import mills.bits.Clops;
import mills.bits.Player;
import mills.position.Positions;
import mills.stones.Stones;
import mills.util.Indexer;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.LongConsumer;
import java.util.stream.IntStream;

public class MapProcessor {

   static final int CHUNK = 16*64;

   final TargetProcessors processors;

   final OpeningMap source;

   final boolean isComplete;

   final Map<Clops, LongConsumer> targets = new ConcurrentSkipListMap<>(Indexer.INDEXED);

   public MapProcessor(TargetProcessors processors, OpeningMap source) {
      this.processors = processors;
      this.source = source;
      this.isComplete = source.isComplete();
   }

   LongConsumer getTarget(Clops clops) {

      LongConsumer result = targets.get(clops);

      if(result==null) {
         synchronized (targets) {
            result = targets.computeIfAbsent(Clops.of(clops), this::newTarget);
         }
      }

      return result;
   }

   private LongConsumer newTarget(Clops clops) {
      return processors.getActor(clops)::set;
   }

   void process() {
      int n = (source.range()+ CHUNK-1)/CHUNK;
      IntStream.range(0, n).parallel().forEach(this::processChunk);
   }

   void processChunk(int chunk) {
      int start = chunk*CHUNK;
      source.index.process(this::process, start, start+CHUNK);
   }

   private void process(int posIndex, long i201) {
      Player player = source.player();
      int stay = Stones.stones(i201, player.opponent());
      int move = Stones.stones(i201, player);
      int closes = Stones.closes(move);
      int free = Stones.STONES ^ (stay | move);
      // if this is complete we need closes only
      if(isComplete)
         free &= closes;

      // place any free stone
      for (int j = free & -free; j != 0; free ^= j, j = free & -free) {
         int moved = move | j;

         if((closes&j)==0)
            process(stay, moved);
         else
            processClosed(stay, moved);
      }
   }

   private void process(int stay, int moved) {
      long m201 = Stones.i201(stay, moved, source.player());
      try {
         set(m201);
      } catch(Throwable e) {
         Stones.i201(stay, moved, source.player());
         throw e;
      }
   }

   private void processClosed(int stay, int moved) {
      for (int m = stay, j = m & -m; j != 0; m ^= j, j = m & -m) {
         process(stay^j, moved);
      }
   }

   public void set(long i201) {
      Clops clops = Positions.clops(i201);
      getTarget(clops).accept(i201);
   }
}
