package mills.index2;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.bits.Sector;
import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static mills.util.AbstractRandomList.virtual;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 13.10.19
 * Time: 11:15
 */
public class FragmentBuilder {

    static final int CLOPS = PopCount.CLOSED.size();
    static final int RADS = Entries.RADIALS.size();

    static final List<EntryTable> EMPTY = virtual(RADS, i -> EntryTable.EMPTY);

    final EntryTables registry;

    final List<List<RingEntry>> fragments = new ArrayList<>();

    final byte[][] backup = new byte[CLOPS][];
    final byte[][] entries = new byte[CLOPS][];

    FragmentBuilder(EntryTables registry) {
        this.registry = registry;
    }

    public void clear() {
        fragments.clear();
        Arrays.fill(entries, null);
        for (byte[] bytes : backup) {
            Arrays.fill(bytes, (byte) 0);
        }
    }

    private void addEntry(RingEntry entry) {
        RingEntry rad = entry.radials();
        addEntry(rad, entry, 0);
    }

    private void addEntry(RingEntry rad, RingEntry entry, int pix) {

        // actual clop count with external rad
        PopCount clop = entry.clop().add(rad.pop);

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

        for (Sector sector : rad.sectors()) {
            RingEntry mirad = rad.withPlayer(sector, Player.None);
            addEntry(mirad, entry, mid);
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
        
        fragments.add(fragment);
        int frix = fragments.size();
        if (frix > Byte.MAX_VALUE)
            throw new IndexOutOfBoundsException("fagments overflow");

        return (byte) frix;
    }

    private byte[] radix(PopCount clop) {
        byte[] radix = entries[clop.index];

        // create on demand
        if (radix == null) {
            radix = new byte[RADS];
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

    public Fragments _build(EntryTable root) {

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

        List<List<EntryTable>> tables = AbstractRandomList.generate(CLOPS, i-> generateTable(entries[i]));
        List<EntryTable> roots = registry.register(fragments);

        return new Fragments(tables, roots);
    }

    private List<EntryTable> generateTable(byte[] entries) {
        if(entries==null)
            return EMPTY;

        assert entries.length == RADS;

        // fill remaining major entries

        for(int i=0; i<RADS; ++i) {
            propagateMajors(entries, i, entries[i]);
        }

        List<EntryTable> table = AbstractRandomList.virtual(RADS, this::getTable);

        return table;
    }

    private EntryTable getTable(int i) {
        if(i==0)
            return EntryTable.EMPTY;

        i = Math.abs(i);
        List<RingEntry> fragment = fragments.get(i);
        return registry.table(fragment);
    }

    void propagateMajors(byte[] entries, int irad, byte frix) {

        if(entries[irad]!=frix) {
            assert entries[irad] == 0;
            entries[irad] = frix;
        }

        if(frix<=0)
            return;

        RingEntry rad = Entries.RADIALS.get(irad);
        for (Sector sector : rad.sectors(Player.None)) {
            RingEntry major = rad.withPlayer(sector, Player.Black);
            propagateMajors(entries, major.index, frix);
            major = rad.withPlayer(sector, Player.White);
            propagateMajors(entries, major.index, frix);
        }
    }

}
