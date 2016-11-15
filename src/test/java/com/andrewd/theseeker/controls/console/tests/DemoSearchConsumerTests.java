package com.andrewd.theseeker.controls.console.tests;

import com.andrewd.theseeker.controls.console.DemoSearchConsumer;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.andrewd.theseeker.controls.console.DemoSearchConsumer.STATUS_PREFIX;

/**
 * Created by Andrew D on 11/14/2016.
 */
public class DemoSearchConsumerTests {

    @Test
    public void MustPrintPushedItemsToTheStream() {
        PrintStream stream = Mockito.mock(PrintStream.class);
        DemoSearchConsumer consumer = new DemoSearchConsumer(stream);

        Path item1 = Paths.get("item1");
        Path item2 = Paths.get("item2");
        Path item3 = Paths.get("item3");

        // Run
        consumer.push(item1);
        consumer.push(item2);
        consumer.push(item3);

        // Verify
        Mockito.verify(stream, Mockito.times(1)).println(Matchers.eq(item1));
        Mockito.verify(stream, Mockito.times(1)).println(Matchers.eq(item2));
        Mockito.verify(stream, Mockito.times(1)).println(Matchers.eq(item3));
    }

    @Test
    public void MustPrintPushedStatusUpdatesToTheStream() {
        PrintStream stream = Mockito.mock(PrintStream.class);
        DemoSearchConsumer consumer = new DemoSearchConsumer(stream);

        // Run
        consumer.pushStatus("item1");
        consumer.pushStatus("item2");
        consumer.pushStatus("item3");

        // Verify
        Mockito.verify(stream, Mockito.times(1)).println(Matchers.eq(STATUS_PREFIX + "item1"));
        Mockito.verify(stream, Mockito.times(1)).println(Matchers.eq(STATUS_PREFIX + "item2"));
        Mockito.verify(stream, Mockito.times(1)).println(Matchers.eq(STATUS_PREFIX + "item3"));
    }
}
