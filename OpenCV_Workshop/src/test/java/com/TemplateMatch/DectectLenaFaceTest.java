package com.TemplateMatch;

import org.junit.Test;
import org.opencv.core.Core;

import static org.junit.Assert.assertNotNull;

public class DectectLenaFaceTest {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    Matching matching = new Matching();


    @Test
    public void FindLenaFace() throws Exception {
        int location[] = matching.getMatchingLocation("screenshots/lena.png", "temp/temp_lena_face.png", "compare/lena_face.png");
        System.out.println("x= "+location[0]+",  y="+location[1]);
        assertNotNull(location);
    }


}
