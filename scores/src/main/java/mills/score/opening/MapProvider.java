package mills.score.opening;

import mills.bits.Clops;
import mills.index.IndexProvider;

import static mills.score.opening.OpeningLayer.MAX_TURN;

public class MapProvider {

   final IndexProvider provider;

   final int turn;

   public MapProvider(IndexProvider provider, int turn) {
      this.provider = provider;
      this.turn = turn;
   }

   OpeningMap createMap(Clops clops) {
      return OpeningMap.open(provider, turn, clops);
   }

   public MapProvider next() {
      if(turn==MAX_TURN)
         return null;

      return new MapProvider(provider, turn+1);
   }
}
