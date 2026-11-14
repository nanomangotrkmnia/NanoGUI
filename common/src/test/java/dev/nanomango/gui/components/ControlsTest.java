package dev.nanomango.gui.components;

import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.test.TestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the interactive behaviour of the form controls.
 */
class ControlsTest {

    private GuiContext context;

    @BeforeEach
    void setUp() {
        context = TestSupport.context();
    }

    // ------------------------------------------------------------------
    // Slider
    // ------------------------------------------------------------------

    @Test
    @DisplayName("the slider clamps the initial value")
    void sliderClampsInitialValue() {
        assertEquals(100.0F, new Slider("A", 0.0F, 100.0F, 500.0F).value());
        assertEquals(0.0F, new Slider("A", 0.0F, 100.0F, -20.0F).value());
        assertEquals(50.0F, new Slider("A", 0.0F, 100.0F, 50.0F).value());
    }

    @Test
    @DisplayName("the slider clamps and reports changed values")
    void sliderValueChanges() {
        List<Float> changes = new ArrayList<>();
        Slider slider = new Slider("Volume", 0.0F, 100.0F, 50.0F, changes::add);

        slider.setValue(75.0F);
        assertEquals(75.0F, slider.value());
        assertEquals(List.of(75.0F), changes);

        slider.setValue(500.0F);
        assertEquals(100.0F, slider.value());
        assertEquals(List.of(75.0F, 100.0F), changes);

        // Setting the same value again does not notify the listener.
        slider.setValue(100.0F);
        assertEquals(2, changes.size());
    }

    @Test
    @DisplayName("the slider snaps the value to the step")
    void sliderStep() {
        Slider slider = new Slider("Volume", 0.0F, 100.0F, 50.0F).step(5.0F);
        slider.setValue(37.0F);
        assertEquals(35.0F, slider.value());
        slider.setValue(38.0F);
        assertEquals(40.0F, slider.value());
    }

    @Test
    @DisplayName("clicking the slider track moves the handle")
    void sliderTrackClick() {
        Slider slider = new Slider("Volume", 0.0F, 100.0F, 0.0F);
        slider.setBounds(0.0F, 0.0F, 200.0F, 24.0F);
        slider.render(context);

        // The handle travels between the left edge and the right edge of the
        // track, both of which are inset by the radius of the handle.
        slider.mouseClicked(100.0F, 12.0F, 0);
        float value = slider.value();
        assertTrue(value > 30.0F && value < 70.0F,
                "the middle of the track must select the middle of the range, was " + value);

        slider.mouseClicked(0.0F, 12.0F, 0);
        assertEquals(0.0F, slider.value());

        slider.mouseClicked(199.0F, 12.0F, 0);
        assertEquals(100.0F, slider.value());
    }

    @Test
    @DisplayName("dragging the slider updates the value")
    void sliderDrag() {
        Slider slider = new Slider("Volume", 0.0F, 100.0F, 0.0F);
        slider.setBounds(0.0F, 0.0F, 200.0F, 24.0F);
        slider.render(context);

        slider.mouseClicked(10.0F, 12.0F, 0);
        slider.mouseDragged(150.0F, 12.0F, 0, 140.0F, 0.0F);
        assertTrue(slider.value() > 50.0F, "the drag must move the handle, was " + slider.value());

        slider.mouseReleased(150.0F, 12.0F, 0);
        float afterRelease = slider.value();
        slider.mouseDragged(10.0F, 12.0F, 0, -140.0F, 0.0F);
        assertEquals(afterRelease, slider.value(), "the slider must not follow the mouse after the release");
    }

    // ------------------------------------------------------------------
    // Dropdown
    // ------------------------------------------------------------------

    @Test
    @DisplayName("the dropdown starts without a selection")
    void dropdownInitialState() {
        Dropdown dropdown = new Dropdown("Mode").options("Default", "Advanced");
        assertEquals(-1, dropdown.selectedIndex());
        assertNull(dropdown.selected());
        assertFalse(dropdown.isOpen());
    }

    @Test
    @DisplayName("selecting an option reports the change")
    void dropdownSelection() {
        AtomicReference<String> selected = new AtomicReference<>();
        Dropdown dropdown = new Dropdown("Mode")
                .options("Default", "Advanced", "Custom")
                .onChange(selected::set);

        dropdown.setSelectedIndex(1);
        assertEquals(1, dropdown.selectedIndex());
        assertEquals("Advanced", dropdown.selected());
        assertEquals("Advanced", selected.get());

        // Out of range indices are ignored.
        dropdown.setSelectedIndex(9);
        dropdown.setSelectedIndex(-1);
        assertEquals(1, dropdown.selectedIndex());
    }

    @Test
    @DisplayName("clicking the dropdown opens it and clicking an option selects it")
    void dropdownInteraction() {
        AtomicReference<String> selected = new AtomicReference<>();
        Dropdown dropdown = new Dropdown("Mode")
                .options("Default", "Advanced")
                .onChange(selected::set);
        dropdown.setBounds(10.0F, 10.0F, 120.0F, 24.0F);
        dropdown.render(context);

        assertTrue(dropdown.mouseClicked(20.0F, 20.0F, 0));
        assertTrue(dropdown.isOpen());

        // The popup is rendered below the control.
        dropdown.render(context);
        float row = context.theme().controlHeight();
        float popupY = 10.0F + 24.0F + 2.0F;
        assertTrue(dropdown.mouseClicked(20.0F, popupY + 2.0F + row * 1.5F, 0));
        assertFalse(dropdown.isOpen());
        assertEquals("Advanced", selected.get());
        assertEquals("Advanced", dropdown.selected());
    }

    @Test
    @DisplayName("clicking outside of the open dropdown closes it")
    void dropdownClickOutsideCloses() {
        Dropdown dropdown = new Dropdown("Mode").options("Default", "Advanced");
        dropdown.setBounds(10.0F, 10.0F, 120.0F, 24.0F);
        dropdown.render(context);

        dropdown.mouseClicked(20.0F, 20.0F, 0);
        assertTrue(dropdown.isOpen());

        dropdown.mouseClicked(400.0F, 400.0F, 0);
        assertFalse(dropdown.isOpen());
    }

    // ------------------------------------------------------------------
    // Toggle and Checkbox
    // ------------------------------------------------------------------

    @Test
    @DisplayName("clicking the toggle flips its value")
    void toggleClick() {
        AtomicInteger changes = new AtomicInteger();
        Toggle toggle = new Toggle("Enabled", false, value -> changes.incrementAndGet());
        toggle.setBounds(0.0F, 0.0F, 80.0F, 24.0F);
        toggle.render(context);

        toggle.mouseClicked(20.0F, 12.0F, 0);
        toggle.mouseReleased(20.0F, 12.0F, 0);
        assertTrue(toggle.value());
        assertEquals(1, changes.get());

        toggle.mouseClicked(20.0F, 12.0F, 0);
        toggle.mouseReleased(20.0F, 12.0F, 0);
        assertFalse(toggle.value());
        assertEquals(2, changes.get());
    }

    @Test
    @DisplayName("the toggle does not change when the value stays the same")
    void toggleSameValue() {
        AtomicInteger changes = new AtomicInteger();
        Toggle toggle = new Toggle("Enabled", true, value -> changes.incrementAndGet());
        toggle.setValue(true);
        assertEquals(0, changes.get());
        toggle.setValue(false);
        assertEquals(1, changes.get());
    }

    @Test
    @DisplayName("clicking the checkbox flips its value")
    void checkboxClick() {
        AtomicInteger changes = new AtomicInteger();
        Checkbox checkbox = new Checkbox("Enabled", false, value -> changes.incrementAndGet());
        checkbox.setBounds(0.0F, 0.0F, 120.0F, 24.0F);
        checkbox.render(context);

        checkbox.mouseClicked(5.0F, 12.0F, 0);
        checkbox.mouseReleased(5.0F, 12.0F, 0);
        assertTrue(checkbox.value());
        assertEquals(1, changes.get());
    }

    @Test
    @DisplayName("a disabled control does not react to clicks")
    void disabledControl() {
        AtomicInteger changes = new AtomicInteger();
        Toggle toggle = new Toggle("Enabled", false, value -> changes.incrementAndGet());
        toggle.setEnabled(false);
        toggle.setBounds(0.0F, 0.0F, 80.0F, 24.0F);
        toggle.render(context);

        assertFalse(toggle.mouseClicked(20.0F, 12.0F, 0));
        assertFalse(toggle.value());
        assertEquals(0, changes.get());
    }

    // ------------------------------------------------------------------
    // Tabs
    // ------------------------------------------------------------------

    @Test
    @DisplayName("the first tab is selected by default")
    void tabsInitialSelection() {
        Tabs tabs = new Tabs();
        tabs.addTab("One", new Text("first"));
        tabs.addTab("Two", new Text("second"));

        assertEquals(0, tabs.selectedIndex());
        assertEquals("One", tabs.selectedTitle());
        assertEquals(2, tabs.titles().size());
    }

    @Test
    @DisplayName("selecting a tab shows only its content")
    void tabsSelection() {
        Tabs tabs = new Tabs();
        var first = tabs.addTab("One", new Text("first"));
        var second = tabs.addTab("Two", new Text("second"));

        assertTrue(first.isVisible());
        assertFalse(second.isVisible());

        tabs.select(1);
        assertEquals(1, tabs.selectedIndex());
        assertEquals("Two", tabs.selectedTitle());
        assertFalse(first.isVisible());
        assertTrue(second.isVisible());

        // Out of range indices are ignored.
        tabs.select(5);
        tabs.select(-1);
        assertEquals(1, tabs.selectedIndex());
    }

    @Test
    @DisplayName("clicking a tab in the header selects it")
    void tabsHeaderClick() {
        Tabs tabs = new Tabs();
        tabs.addTab("One", new Text("first"));
        tabs.addTab("Two", new Text("second"));
        tabs.setBounds(0.0F, 0.0F, 400.0F, 300.0F);
        tabs.render(context);

        float firstWidth = context.font().width("One") + context.theme().padding() * 2.0F;
        float headerHeight = context.theme().controlHeight() + 4.0F;

        // The second tab starts where the first one ends.
        assertTrue(tabs.mouseClicked(firstWidth + 2.0F, headerHeight * 0.5F, 0));
        assertEquals(1, tabs.selectedIndex());

        // A click on the first tab selects it again.
        assertTrue(tabs.mouseClicked(firstWidth * 0.5F, headerHeight * 0.5F, 0));
        assertEquals(0, tabs.selectedIndex());
    }

    @Test
    @DisplayName("a click below the header does not change the selection")
    void tabsClickBelowHeader() {
        Tabs tabs = new Tabs();
        tabs.addTab("One", new Text("first"));
        tabs.addTab("Two", new Text("second"));
        tabs.setBounds(0.0F, 0.0F, 400.0F, 300.0F);
        tabs.render(context);

        float firstWidth = context.font().width("One") + context.theme().padding() * 2.0F;
        float headerHeight = context.theme().controlHeight() + 4.0F;

        // The click is inside the content area, so it is forwarded to the
        // content of the active tab and cannot change the selection.
        tabs.mouseClicked(firstWidth + 2.0F, headerHeight + 20.0F, 0);
        assertEquals(0, tabs.selectedIndex());
    }
}
