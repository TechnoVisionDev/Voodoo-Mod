package com.technovision.voodoo.client;

import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** The original manual and recipes, paginated using the player's actual font. */
public final class VoodooManual {
    public static void open() {
        Minecraft minecraft = Minecraft.getInstance();
        List<Component> pages = new ArrayList<>();
        try (var stream = VoodooManual.class.getResourceAsStream("/assets/voodoo/manual.json")) {
            if (stream == null) throw new IOException("Missing Voodoo manual");
            JsonArray entries = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonArray();
            int indexPages = (entries.size() + 7) / 8;
            for (int i = 0; i < indexPages; i++) pages.add(Component.literal("Voodoo Manual\n\n"));
            for (int i = 0; i < entries.size(); i++) {
                JsonObject entry = entries.get(i).getAsJsonObject();
                String title = entry.get("title").getAsString();
                int pageNumber = pages.size() + 1;
                ((MutableComponent)pages.get(i / 8)).append(Component.literal(title + "\n").withStyle(style -> style.withClickEvent(new ClickEvent.ChangePage(pageNumber)).withUnderlined(true)));
                for (var section : entry.getAsJsonArray("sections")) {
                    List<String> lines = new ArrayList<>();
                    minecraft.font.getSplitter().splitLines(title + "\n\n" + section.getAsString(), 114, Style.EMPTY).forEach(line -> lines.add(line.getString()));
                    for (int offset = 0; offset < lines.size(); offset += 13)
                        pages.add(Component.literal(String.join("\n", lines.subList(offset, Math.min(offset + 13, lines.size())))));
                }
            }
        } catch (IOException exception) { throw new IllegalStateException("Cannot read Voodoo manual", exception); }
        minecraft.gui.setScreen(new BookViewScreen(new BookViewScreen.BookAccess(pages)));
    }
}
