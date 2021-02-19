package com.infilect.taskcamera.helper.camera;

import com.infilect.taskcamera.R;

import static androidx.camera.core.ImageCapture.FLASH_MODE_AUTO;
import static androidx.camera.core.ImageCapture.FLASH_MODE_OFF;
import static androidx.camera.core.ImageCapture.FLASH_MODE_ON;

public class FlashModes {
    private static FlashModes head;

    public static void init() {
        head = new FlashModes(FLASH_MODE_AUTO, R.drawable.ic_flash_auto);
        FlashModes first = new FlashModes(FLASH_MODE_OFF, R.drawable.ic_flash_off);
        head.setNext(first);
        FlashModes second = new FlashModes(FLASH_MODE_ON, R.drawable.ic_flash_on);
        first.setNext(second);
        second.setNext(head);
    }

    final int flashMode;
    final int imgRes;
    FlashModes next;

    private FlashModes(int flashMode, int res) {
        this.flashMode = flashMode;
        this.imgRes = res;
    }

    public void setNext(FlashModes next) {
        this.next = next;
    }

    public int getValue() {
        return flashMode;
    }

    public int getDrawable() {
        return imgRes;
    }

    public FlashModes getNext() {
        return next;
    }

    public static FlashModes getHead() {
        return head;
    }
}
