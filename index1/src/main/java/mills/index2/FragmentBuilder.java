package mills.index2;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.bits.Sector;
import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;
import mills.util.ArraySet;

import java.util.*;

import static mills.index2.Fragments.CLOPS;
import static mills.index2.Fragments.RADS;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 13.10.19
 * Time: 11:15
 */
public class FragmentBuilder {

    final EntryTables registry;

    final List<List<RingEntry>> fragments = new ArrayList<>();

    final List<byte[]> backup = new ArrayList<>();
    final byte[][] entries = new byte[CLOPS][];

    FragmentBuilder(EntryTables registry) {
        this.registry = registry;
    }

    public void clear() {
        fragments.clear();

        for (int i = 0; i < entries.length; i++) {
            byte[] radix = entries[i];
            if(radix!=null) {
                Arrays.fill(radix, (byte) 0);
                backup.add(radix);
                entries[i] = null;
            }
        }
    }

    private void addEntry(RingEntry entry) {
        addEntry(Entries.EMPTY, entry, 0);
    }

    private void addEntry(RingEntry rad, RingEntry entry, int pix) {

        // actual clop count with external rad
        PopCount clop = entry.clop().add(entry.and(rad).pop);

        // ain't no clop>4
        if(clop.max()>4)
            return;

        byte[] radix = radix(clop);

        byte frix = radix[rad.index];

        // still unassigned
        if(frix==0) {
            if(pix==0) {
                // start as a new direct fragment
                frix = copyFragment(0);
            } else {
                // continue as indirect reference
                frix = (byte) -pix;
            }
            radix[rad.index] = (byte) frix;
        } else if(frix<0) {
            // if not a reference of pix
            if (-frix != pix) {
                // indirect referenced must be copied
                // and start as a new direct reference
                frix = copyFragment(-frix);
            }
        }

        // any direct reference either directly or after copy
        // may be extended directly.
        if(frix>0) {
            fragments.get(frix).add(entry);
        } else {
            // this is an indirect reference already containing element.
            assert verify(-frix, entry);
        }
        
        // propagate frix as reference to all minors
        int mid = Math.abs(frix);

        for (Sector sector : rad.sectors(Player.None).radials()) {
            RingEntry added = rad.withPlayer(sector, Player.Black);
            addEntry(added, entry, mid);
            added = rad.withPlayer(sector, Player.White);
            addEntry(added, entry, mid);
        }
    }

    private boolean verify(int frix, RingEntry entry) {
        if(frix<1)
            return false;

        List<RingEntry> fragment = fragments.get(frix);
        int size = fragment.size();
        return fragment.get(size-1).equals(entry);
    }

    private byte copyFragment(int pid) {
        List<RingEntry> parent = pid==0 ? Collections.emptyList(): fragments.get(pid);

        int size = parent.size();
        size += size/2;
        if(size<8)
            size = 8;

        List<RingEntry> fragment = new ArrayList<>(size);
        fragment.addAll(parent);
        
        int frix = fragments.size();
        if (frix > Byte.MAX_VALUE)
            throw new IndexOutOfBoundsException("fagments overflow");

        fragments.add(fragment);

        return (byte) frix;
    }

    private byte[] radix(PopCount clop) {
        byte[] radix = entries[clop.index];

        // create on demand
        if (radix == null) {
            if(backup.isEmpty())
                radix = new byte[RADS];
            else
                radix = backup.remove(backup.size()-1);

            entries[clop.index] = radix;
        }

        return radix;
    }

    public Fragments build(EntryTable root) {
        try {
            return _build(root);
        } finally {
            clear();
        }
    }

    private static final Map<RingEntry, EntryTable> EMPTY_FRAGMENT
            = ArraySet.of(Entries::of, AbstractRandomList.constant(RADS, EntryTable.EMPTY), EntryTable.EMPTY).asMap();

    Fragments _build(EntryTable root) {

        if(!fragments.isEmpty())
            throw new IllegalStateException("fragment builder in use");

        fragments.add(root);

        root.forEach(this::addEntry);

        // normalize referenced entry tables.
        for(int i=0; i<fragments.size(); ++i) {
            List<RingEntry> fragment = fragments.get(i);
            fragment = registry.table(fragment);
            fragments.set(i, fragment);
        }

        // convert all entry arrays into entry tables

        List<EntryTable> roots = registry.register(fragments);

        List<Map<RingEntry, EntryTable>> tables = AbstractRandomList.generate(CLOPS, i-> generateTable(entries[i]));
        Map<PopCount, Map<RingEntry, EntryTable>> map = ArraySet.of(PopCount.TABLE::get, tables, EMPTY_FRAGMENT).asMap();

        return new Fragments(map, roots);
    }

    private Map<RingEntry, EntryTable> generateTable(byte[] entries) {
        if(entries==null)
            return null;

        assert entries.length == RADS;

        List<EntryTable> table = AbstractRandomList.virtual(RADS, i -> getTable(entries[i]));
        table = registry.register(table);

        return ArraySet.of(Entries::of, table, EntryTable.EMPTY).asMap();
    }

    private EntryTable getTable(int i) {
        if(i==0)
            return EntryTable.EMPTY;

        i = Math.abs(i);
        List<RingEntry> fragment = fragments.get(i);
        return registry.table(fragment);
    }
}
