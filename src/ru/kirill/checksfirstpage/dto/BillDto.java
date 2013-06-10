package ru.kirill.checksfirstpage.dto;

import java.util.Date;

public class BillDto {

	public String id;
	public String cash;
	public Date payDate;
	public String kind;
	public String description;
    public String uuid;
    public int expImp; // 0 - новый, 1 - отправлен на почту, 2 - загружен из письма
    public Date inputDate;
}
