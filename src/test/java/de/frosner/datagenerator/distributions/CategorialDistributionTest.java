package de.frosner.datagenerator.distributions;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

import net.sf.qualitycheck.exception.IllegalInstanceOfArgumentException;

import org.fest.assertions.Delta;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import de.frosner.datagenerator.features.DiscreteFeatureValue;
import de.frosner.datagenerator.features.FeatureValue;
import de.frosner.datagenerator.util.StatisticsTestUtil;

public class CategorialDistributionTest {

	private CategorialDistribution _distribution;

	@Before
	public void createDistribution() {
		_distribution = new CategorialDistribution(Lists.newArrayList(0.6, 0.3, 0.1));
	}

	@Test
	public void testGetProbabilityOf() {
		assertThat(_distribution.getProbabilityOf(new DiscreteFeatureValue(0))).isEqualTo(0.6);
		assertThat(_distribution.getProbabilityOf(new DiscreteFeatureValue(1))).isEqualTo(0.3);
		assertThat(_distribution.getProbabilityOf(new DiscreteFeatureValue(2))).isEqualTo(0.1);

		assertThat(_distribution.getProbabilityOf(new DiscreteFeatureValue(-1))).isEqualTo(0);
		assertThat(_distribution.getProbabilityOf(new DiscreteFeatureValue(3))).isEqualTo(0);
	}

	@Test
	public void testSample() {
		_distribution.setSeed(43253);
		assertThat(_distribution.sample()).isInstanceOf(DiscreteFeatureValue.class);
		List<Double> samples = Lists.newArrayList();
		for (int i = 0; i < 100000; i++) {
			samples.add((double) (Integer) _distribution.sample().getValue());
		}
		double sampleMean = StatisticsTestUtil.sampleMean(samples);
		assertThat(sampleMean).isEqualTo(0.5, Delta.delta(0.01));
	}

	@Test(expected = IllegalInstanceOfArgumentException.class)
	public void testGetProbabilityOf_wrongFeatureValueType() {
		FeatureValue wrongFeatureValueType = new FeatureValue() {
			@Override
			public Object getValue() {
				return null;
			}

			@Override
			public String getValueAsString() {
				return null;
			}
		};
		_distribution.getProbabilityOf(wrongFeatureValueType);
	}
}
