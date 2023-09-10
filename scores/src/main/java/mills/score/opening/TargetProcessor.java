package mills.score.opening;

import mills.bits.Clops;
import mills.bits.Player;
import mills.position.Positions;
import mills.stones.Stones;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Class TargetProcessor processes a single source map against a set of target maps.
 */
public class TargetProcessor {

   static final int CHUNK = 16*64;

   final OpeningMaps target;

   final OpeningMap source;

   final boolean isComplete;

   AtomicInteger done = new AtomicInteger();

   public TargetProcessor(OpeningMaps target, OpeningMap source) {
      this.target = target;
      this.source = source;
      this.isComplete = source.isComplete();
   }

   OpeningMap getTarget(Clops clops) {
      return target.openMap(clops);
   }

   int chunks() {
      return (source.range()+ CHUNK-1)/CHUNK;
   }

   void process() {
      int n = chunks();
      IntStream.range(0, n).parallel().forEach(this::processChunk);
   }

   @Override
   public String toString() {
      return String.format("MapProcessor(%d/%d)", done.get(), chunks());
   }

   void processChunk(int chunk) {
      int start = chunk*CHUNK;
      source.index.process(this::process, start, start+CHUNK);
      done.incrementAndGet();
   }

   private void process(int posIndex, long i201) {
      if(!source.get(posIndex))
         return;

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
      getTarget(clops).submit(i201);
   }
}
