package mills.main;

import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.ring.RingEntry;

import java.util.function.ToIntFunction;

public class Quads {

    private static boolean isQuad(RingEntry entry) {
        int rad = entry.radix();
        return entry.index == 82*rad;
    }

    EntryTable quads = Entries.TABLE.filter(Quads::isQuad);

    void dump(ToIntFunction<RingEntry> mask, String name) {

        System.out.println("name: " + name);
        System.out.println("  0 1 2 3 4 5 6 7 8  0 1 2 3 4 5 6 7 8");

        for(int i=0; i<9; ++i) {
            System.out.print(i);
            for(int j=0; j<9; ++j) {
                RingEntry quad = quads.get(9*j+i);
                int ml = mask.applyAsInt(quad) & 0xf;
                System.out.format(" %1x", ml);
            }

            System.out.print(" ");
            for(int j=0; j<9; ++j) {
                RingEntry quad = quads.get(9*j+i);
                int ml = (mask.applyAsInt(quad)>>4) & 0xf;
                System.out.format(" %1x", ml);
            }
            System.out.println();
        }
        System.out.println();
    }

    void run() {
        dump(RingEntry::pmeq, "pmeq");
        dump(RingEntry::pmin, "pmin");
        dump(RingEntry::pmlt, "pmlt");
    }

    public static void main(String[] args) {
        new Quads().run();
    }

}
