package com.andrewd.theseeker.controls.console.tests;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Andrew D on 11/14/2016.
 */
class InputStreamFake extends InputStream {

    private char[] buffer = null;
    private int position = 0;

    InputStreamFake(String string) {
        buffer = string.toCharArray();
    }

    @Override
    public int read() throws IOException {
        if (isBufferEmpty()) {
            return 0;
        }

        return buffer[position++];
    }

    private boolean isBufferEmpty() {
        return buffer == null || buffer.length == 0 ||
                (position == buffer.length);
    }
}