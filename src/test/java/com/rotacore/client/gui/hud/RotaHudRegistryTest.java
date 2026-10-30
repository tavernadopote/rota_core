package com.rotacore.client.gui.hud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RotaHudRegistryTest {

    @BeforeEach
    public void setup() {
        RotaHudRegistry.clear();
    }

    private static class DummyHudElement implements IRotaHudElement {
        private final String id;
        private final boolean visible;

        DummyHudElement(String id, boolean visible) {
            this.id = id;
            this.visible = visible;
        }

        @Override public String getId() { return id; }
        @Override public Component getDisplayName() { return Component.literal(id); }
        @Override public RotaHudAnchor getDefaultAnchor() { return RotaHudAnchor.TOP_RIGHT; }
        @Override public RotaViewportShape getShape() { return RotaViewportShape.CIRCLE; }
        @Override public int getWidth() { return 64; }
        @Override public int getHeight() { return 64; }
        @Override public boolean isVisible() { return visible; }
        @Override public void render(GuiGraphics graphics, int x, int y, int width, int height, float partialTick) {}
    }

    @Test
    public void testRegisterAndGet() {
        DummyHudElement elem = new DummyHudElement("test_mod:element", true);
        RotaHudRegistry.register(elem);

        assertTrue(RotaHudRegistry.hasElement("test_mod:element"));
        assertSame(elem, RotaHudRegistry.getElement("test_mod:element"));
        assertEquals(1, RotaHudRegistry.getCount());
    }

    @Test
    public void testUnregister() {
        DummyHudElement elem = new DummyHudElement("test_mod:removable", true);
        RotaHudRegistry.register(elem);
        assertTrue(RotaHudRegistry.hasElement("test_mod:removable"));

        RotaHudRegistry.unregister("test_mod:removable");
        assertFalse(RotaHudRegistry.hasElement("test_mod:removable"));
        assertNull(RotaHudRegistry.getElement("test_mod:removable"));
        assertEquals(0, RotaHudRegistry.getCount());
    }

    @Test
    public void testGetVisibleElements() {
        DummyHudElement e1 = new DummyHudElement("e1", true);
        DummyHudElement e2 = new DummyHudElement("e2", false);
        DummyHudElement e3 = new DummyHudElement("e3", true);

        RotaHudRegistry.register(e1);
        RotaHudRegistry.register(e2);
        RotaHudRegistry.register(e3);

        assertEquals(3, RotaHudRegistry.getAllElements().size());

        List<IRotaHudElement> visible = RotaHudRegistry.getVisibleElements();
        assertEquals(2, visible.size());
        assertTrue(visible.contains(e1));
        assertFalse(visible.contains(e2));
        assertTrue(visible.contains(e3));
    }

    @Test
    public void testNullSafety() {
        assertDoesNotThrow(() -> RotaHudRegistry.register(null));
        assertDoesNotThrow(() -> RotaHudRegistry.unregister(null));
        assertNull(RotaHudRegistry.getElement(null));
        assertFalse(RotaHudRegistry.hasElement(null));
    }
}
