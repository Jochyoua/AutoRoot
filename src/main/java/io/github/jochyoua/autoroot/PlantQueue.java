package io.github.jochyoua.autoroot;

import lombok.Getter;
import org.bukkit.entity.Item;

@Getter
public class PlantQueue {
    public final Item item;
    final PlantableRule rule;
    private int ticksRemaining;

    public PlantQueue(Item item, PlantableRule rule, int delay) {
        this.item = item;
        this.rule = rule;
        this.ticksRemaining = delay;
    }

    public void decrementTicks() {
        ticksRemaining--;
    }

    public boolean isReady() {
        return ticksRemaining <= 0;
    }
}
