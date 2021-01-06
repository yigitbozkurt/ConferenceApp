package com.yigit.conferenceapp.ui.main.ui.home;

import com.yigit.conferenceapp.data.model.seminar.SeminarModel;

public interface onItemClick {
    //Anasayfa veya kayıtlı seminerlerimde bir iteme tıklandığında tetiklemek için kullanıyoruz.
    void itemClick(SeminarModel seminar);
}
