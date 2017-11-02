package org.deeplearning4j.nn.conf.layers;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.ParamInitializer;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.distribution.Distribution;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.layers.convolution.Deconvolution2DLayer;
import org.deeplearning4j.nn.params.DeconvolutionParamInitializer;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.IterationListener;
import org.deeplearning4j.util.ConvolutionUtils;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.activations.IActivation;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.Collection;
import java.util.Map;

/**
 * 2D deconvolution layer configuration
 *
 * @author Max Pumperla
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Deconvolution2D extends ConvolutionLayer {

    /**
     * Deconvolution2D layer
     * nIn in the input layer is the number of channels
     * nOut is the number of filters to be used in the net or in other words the depth
     * The builder specifies the filter/kernel size, the stride and padding
     * The pooling layer takes the kernel size
     */
    protected Deconvolution2D(BaseConvBuilder<?> builder) {
        super(builder);
        this.hasBias = builder.hasBias;
        this.convolutionMode = builder.convolutionMode;
        this.dilation = builder.dilation;
        if (builder.kernelSize.length != 2)
            throw new IllegalArgumentException("Kernel size of should be rows x columns (a 2d array)");
        this.kernelSize = builder.kernelSize;
        if (builder.stride.length != 2)
            throw new IllegalArgumentException("Stride should include stride for rows and columns (a 2d array)");
        this.stride = builder.stride;
        if (builder.padding.length != 2)
            throw new IllegalArgumentException("Padding should include padding for rows and columns (a 2d array)");
        this.padding = builder.padding;
        this.cudnnAlgoMode = builder.cudnnAlgoMode;
        this.cudnnFwdAlgo = builder.cudnnFwdAlgo;
        this.cudnnBwdFilterAlgo = builder.cudnnBwdFilterAlgo;
        this.cudnnBwdDataAlgo = builder.cudnnBwdDataAlgo;

        initializeConstraints(builder);
    }

    public boolean hasBias(){
        return hasBias;
    }

    @Override
    public Deconvolution2D clone() {
        Deconvolution2D clone = (Deconvolution2D) super.clone();
        if (clone.kernelSize != null)
            clone.kernelSize = clone.kernelSize.clone();
        if (clone.stride != null)
            clone.stride = clone.stride.clone();
        if (clone.padding != null)
            clone.padding = clone.padding.clone();
        return clone;
    }

    @Override
    public Layer instantiate(NeuralNetConfiguration conf, Collection<IterationListener> iterationListeners,
                             int layerIndex, INDArray layerParamsView, boolean initializeParams) {
        LayerValidation.assertNInNOutSet("Deconvolution2D", getLayerName(), layerIndex, getNIn(), getNOut());

        org.deeplearning4j.nn.layers.convolution.Deconvolution2DLayer ret =
                new org.deeplearning4j.nn.layers.convolution.Deconvolution2DLayer(conf);
        ret.setListeners(iterationListeners);
        ret.setIndex(layerIndex);
        ret.setParamsViewArray(layerParamsView);
        Map<String, INDArray> paramTable = initializer().init(conf, layerParamsView, initializeParams);
        ret.setParamTable(paramTable);
        ret.setConf(conf);
        return ret;
    }

    @Override
    public ParamInitializer initializer() {
        return DeconvolutionParamInitializer.getInstance();
    }

    @Override
    public InputType getOutputType(int layerIndex, InputType inputType) {
        if (inputType == null || inputType.getType() != InputType.Type.CNN) {
            throw new IllegalStateException("Invalid input for Convolution layer (layer name=\"" + getLayerName()
                    + "\"): Expected CNN input, got " + inputType);
        }

        return InputTypeUtil.getOutputTypeCnnLayers(inputType, kernelSize, stride, padding, dilation,
                convolutionMode, nOut, layerIndex, getLayerName(), Deconvolution2DLayer.class);
    }


    public static class Builder extends BaseConvBuilder<Builder> {

        public Builder(int[] kernelSize, int[] stride, int[] padding) {
            super(kernelSize, stride, padding);
        }

        public Builder(int[] kernelSize, int[] stride) {
            super(kernelSize, stride);
        }

        public Builder(int... kernelSize) {
            super(kernelSize);
        }

        public Builder() {
            super();
        }

        /**
         * Set the convolution mode for the Convolution layer.
         * See {@link ConvolutionMode} for more details
         *
         * @param convolutionMode Convolution mode for layer
         */
        @Override
        public Builder convolutionMode(ConvolutionMode convolutionMode) {
            this.convolutionMode = convolutionMode;
            return this;
        }

        @Override
        public Builder nIn(int nIn) {
            super.nIn(nIn);
            return this;
        }

        @Override
        public Builder nOut(int nOut) {
            super.nOut(nOut);
            return this;
        }

        /**
         * Defaults to "PREFER_FASTEST", but "NO_WORKSPACE" uses less memory.
         *
         * @param cudnnAlgoMode
         */
        @Override
        public Builder cudnnAlgoMode(AlgoMode cudnnAlgoMode) {
            super.cudnnAlgoMode(cudnnAlgoMode);
            return this;
        }

        /**
         * Layer name assigns layer string name.
         * Allows easier differentiation between layers.
         *
         * @param layerName
         */
        @Override
        public Builder name(String layerName) {
            super.name(layerName);
            return this;
        }

        @Override
        public Builder activation(IActivation activationFunction) {
            super.activation(activationFunction);
            return this;
        }

        @Override
        public Builder activation(Activation activation) {
            super.activation(activation);
            return this;
        }

        /**
         * Weight initialization scheme.
         *
         * @param weightInit
         * @see WeightInit
         */
        @Override
        public Builder weightInit(WeightInit weightInit) {
            super.weightInit(weightInit);
            return this;
        }

        @Override
        public Builder biasInit(double biasInit) {
            super.biasInit(biasInit);
            return this;
        }

        /**
         * Distribution to sample initial weights from. Used in conjunction with
         * .weightInit(WeightInit.DISTRIBUTION).
         *
         * @param dist
         */
        @Override
        public Builder dist(Distribution dist) {
            super.dist(dist);
            return this;
        }

        /**
         * L1 regularization coefficient (weights only). Use {@link #l1Bias(double)} to configure the l1 regularization
         * coefficient for the bias.
         *
         * @param l1 L1 regularization coefficient
         */
        @Override
        public Builder l1(double l1) {
            return super.l1(l1);
        }

        /**
         * L2 regularization coefficient (weights only). Use {@link #l2Bias(double)} to configure the l2 regularization
         * coefficient for the bias.
         *
         * @param l2 L2 regularization coefficient
         */
        @Override
        public Builder l2(double l2) {
            return super.l2(l2);
        }

        /**
         * L1 regularization coefficient for the bias. Default: 0. See also {@link #l1(double)}
         *
         * @param l1Bias L1 regularization coefficient (bias)
         */
        @Override
        public Builder l1Bias(double l1Bias) {
            return super.l1Bias(l1Bias);
        }

        /**
         * L2 regularization coefficient for the bias. Default: 0. See also {@link #l2(double)}
         *
         * @param l2Bias
         */
        @Override
        public Builder l2Bias(double l2Bias) {
            return super.l2Bias(l2Bias);
        }

        /**
         * Gradient updater. For example, SGD for standard stochastic gradient descent, NESTEROV for Nesterov momentum,
         * RSMPROP for RMSProp, etc.
         *
         * @param updater
         * @see Updater
         */
        @Override
        @Deprecated
        public Builder updater(Updater updater) {
            return super.updater(updater);
        }

        /**
         * Gradient normalization strategy. Used to specify gradient renormalization, gradient clipping etc.
         *
         * @param gradientNormalization Type of normalization to use. Defaults to None.
         * @see GradientNormalization
         */
        @Override
        public Builder gradientNormalization(GradientNormalization gradientNormalization) {
            super.gradientNormalization(gradientNormalization);
            return this;
        }

        /**
         * Threshold for gradient normalization, only used for GradientNormalization.ClipL2PerLayer,
         * GradientNormalization.ClipL2PerParamType, and GradientNormalization.ClipElementWiseAbsoluteValue<br>
         * Not used otherwise.<br>
         * L2 threshold for first two types of clipping, or absolute value threshold for last type of clipping.
         *
         * @param threshold
         */
        @Override
        public Builder gradientNormalizationThreshold(double threshold) {
            super.gradientNormalizationThreshold(threshold);
            return this;
        }

        /**
         * Size of the convolution
         * rows/columns
         * @param kernelSize the height and width of the
         *                   kernel
         * @return
         */
        public Builder kernelSize(int... kernelSize) {
            this.kernelSize = kernelSize;
            return this;
        }

        public Builder stride(int... stride) {
            this.stride = stride;
            return this;
        }

        public Builder padding(int... padding) {
            this.padding = padding;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Deconvolution2D build() {
            ConvolutionUtils.validateCnnKernelStridePadding(kernelSize, stride, padding);

            return new Deconvolution2D(this);
        }
    }

}