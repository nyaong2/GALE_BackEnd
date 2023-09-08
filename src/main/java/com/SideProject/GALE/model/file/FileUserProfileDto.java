package com.SideProject.GALE.model.file;

import java.sql.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FileUserProfileDto {
	private String userid;
	private String filename;
	private String stored_filename;
	private Date regdate;
}
