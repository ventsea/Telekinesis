package com.st.letter.lib.media;

import java.io.Serializable;

public interface LocalBean extends Serializable {

    void buildCorrectFileBean(String ip, int port);
}
