package mills.score.opening;

import mills.bits.Clops;
import mills.bits.Player;
import mills.position.Positions;
import mills.stones.Stones;
import mills.util.Indexer;

import java.util.*;
import java.util.function.LongConsumer;
import java.util.stream.IntStream;

public class MapProcessor {

   static final int CHUNK = 16*64;

   final TargetProcessors processors;

   final OpeningMap source;

   final Map<Clops, LongConsumer> targets = new TreeMap<>(Indexer.INDEXED);

   public MapProcessor(TargetProcessors processors, OpeningMap source) {
      this.processors = processors;
      this.source = source;
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
      int free = Stones.STONES ^ (stay | move);
      int closes = Stones.closes(move);

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
      set(m201);
   }

   private void processClosed(int stay, int moved) {
      for (int m = stay, j = m & -m; j != 0; m ^= j, j = m & -m) {
         process(stay^j, moved);
      }
   }

   public void set(long i201) {
      Clops clops = Positions.clops(i201);
      targets.computeIfAbsent(clops, this::newTarget);
   }
}
