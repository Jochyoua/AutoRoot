package io.github.jochyoua.autoroot.enums;

import lombok.Getter;

@Getter
public enum LeafFailReasonEnum {

    NULL("Leaf was null"),
    DEAD_TREE("Tree is not alive near leaf"),
    BARE_CANOPY("Tree canopy is bare"),
    BAD_BIOME("Biome is not allowed"),
    NOT_ENABLED("This sapling is not configured to enable seed falling"),
    CHANCE_FAIL("Chance roll exceeded natural fall chance"),
    OK("Leaf passed all checks");

    private final String debugMessage;

    LeafFailReasonEnum(String debugMessage) {
        this.debugMessage = debugMessage;
    }

}
