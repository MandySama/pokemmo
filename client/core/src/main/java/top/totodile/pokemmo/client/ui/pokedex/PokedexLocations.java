package top.totodile.pokemmo.client.ui.pokedex;

import de.matthiasmann.twl.*;

import java.util.*;

/** Shared label columns from PokedexWindow.qx; the three time columns can share one value. */
final class PokedexLocations extends DialogLayout {
    private final List<Widget[]> rows = new ArrayList<>();
    private final Group[] columns = new Group[7];
    private final Group times = createParallelGroup(), vertical = createSequentialGroup();
    private static final String[] SIZES = {
        "medium", "small", "name", "smallest", "smallest", "smallest", "smallest"
    };

    PokedexLocations() {
        setTheme("/pokedex-location-grid");
        setAddDefaultGaps(false);
        for (int i = 0; i < columns.length; i++) columns[i] = createParallelGroup();
        var horizontal = createSequentialGroup();
        for (int i = 0; i < 4; i++) {
            if (i > 0) horizontal.addGap(DEFAULT_GAP);
            horizontal.addGroup(columns[i]);
        }
        times.addGroup(
                createSequentialGroup()
                        .addGroup(columns[4])
                        .addGap(DEFAULT_GAP)
                        .addGroup(columns[5])
                        .addGap(DEFAULT_GAP)
                        .addGroup(columns[6]));
        horizontal.addGap(DEFAULT_GAP).addGroup(times);
        setHorizontalGroup(horizontal);
        setVerticalGroup(vertical);
    }

    void titles(String... values) {
        row(values, true);
    }

    void values(String... values) {
        row(values, false);
    }

    private void row(String[] values, boolean header) {
        if (!rows.isEmpty()) vertical.addGap(DEFAULT_GAP);
        var cells = new Widget[values.length];
        var line = createParallelGroup();
        for (int i = 0; i < values.length; i++) {
            // Reference PokedexWindow.qx uses single-line labels: natural text
            // width is the preferred width; theme minWidth is only a minimum.
            Label cell = new Label(values[i]);
            cells[i] = cell;
            cell.setTheme("/label-" + (header ? "title-dex-" : "value-dex-") + SIZES[i]);
            if (values.length == 5 && i == 4) times.addWidget(cell);
            else columns[i].addWidget(cell);
            line.addWidget(cell);
        }
        rows.add(cells);
        vertical.addGroup(line);
    }
}
