package tech.konata.phosphate.utils.dnd;

import lombok.Getter;
import tech.konata.phosphate.event.events.input.FileDraggedInEvent;
import tech.konata.phosphate.event.events.input.FileDroppedEvent;
import tech.konata.phosphate.management.EventManager;

import java.util.Arrays;
import java.util.List;

/**
 * @author IzumiiKonata
 * Date: 2025/2/4 10:04
 */
public class DropTargetHandler {

    @Getter
    private static final DropTargetHandler instance = new DropTargetHandler();

    @Getter
    private boolean isDragging = false;

    public DropTargetHandler() {

    }

    public void onDragEnterImpl(int x, int y, int effect, String[] formats) {
        this.isDragging = true;

        List<String> fileNames = Arrays.asList(formats);
        EventManager.call(new FileDraggedInEvent(fileNames));
    }

    public void onDragLeaveImpl() {
        this.isDragging = false;
    }

    public void onDragOverImpl(int x, int y, int effect) {

    }

    public void onDropImpl(int x, int y, int effect, String[] formats) {
        List<String> fileNames = Arrays.asList(formats);
        EventManager.call(new FileDroppedEvent(fileNames));
        this.isDragging = false;
    }


}
