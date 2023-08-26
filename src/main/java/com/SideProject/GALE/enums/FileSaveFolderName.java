package com.SideProject.GALE.enums;

public enum FileSaveFolderName {
	Healing(1),
	Culture(2),
	Sightseeing(3),
	Event(4);
	
    private final int value;

    FileSaveFolderName(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}