package org.deeplearning4j.datasets.savers;

import org.apache.commons.io.FilenameUtils;
import org.datavec.image.loader.CifarLoader;
import org.deeplearning4j.datasets.iterator.MultipleEpochsIterator;
import org.deeplearning4j.datasets.iterator.impl.CifarDataSetIterator;
import org.junit.Ignore;
import org.junit.Test;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

/**
 * Created by kepricon on 17. 4. 19.
 */
public class DataSetSaverTest {

    @Ignore
    @Test
    public void testDataSetSaver() {
        int height = 250;
        int width = 250;
        int channels = 3;
        int batchSize = 32;
        int totalExamples = CifarLoader.NUM_TRAIN_IMAGES;
        DataSetIterator iter = new CifarDataSetIterator(batchSize, totalExamples, new int[] {height, width, channels});
        iter = new MultipleEpochsIterator(iter, 500);

        // if you want to set manually the number of threads
        // call .setNumThreads like below
        // unless it DataSetSaver uses default number of thread pool(cores *2)
        // DataSetSaver.setNumThreads(20);

        String savePath = FilenameUtils.concat(System.getProperty("java.io.tmpdir"),  "dl4j-cifar10");
        DataSetSaver.saveDataSets(iter, savePath);
    }
}