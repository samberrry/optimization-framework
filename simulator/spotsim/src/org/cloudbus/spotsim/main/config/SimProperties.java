package org.cloudbus.spotsim.main.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Properties;

import org.cloudbus.spotsim.enums.WorkflowPolicies;
import org.cloudbus.cloudsim.util.workload.PerformanceVariatorEnum;
import org.cloudbus.spotsim.broker.forecasting.PriceForecastKey;
import org.cloudbus.spotsim.enums.BiddingStrategy;
import org.cloudbus.spotsim.enums.FaultToleranceMethod;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.PriceForecastingMethod;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.enums.RuntimeEstimationMethod;
import org.cloudbus.spotsim.main.TraceDistribution;
import org.cloudbus.spotsim.pricing.PriceTraceGen;

/**
 * @see SimPropertiesTest
 */
public enum SimProperties implements SimProp {

	DC_VM_INIT_TIME("spotsim.dc.vm.init.time", 100),

	DC_VM_MAX("spotsim.dc.vm.max", 500000),

	DC_DEFAULT_OS("spotsim.dc.default.os", OS.LINUX),

	DC_DEFAULT_REGION("spotsim.dc.region", Region.DEEPAK_TEST),

	DUMMY("spotsim.unset"),

	DUMMY2("spotsim.unset2", PriceForecastKey.class),

	FT_CKPT_FREQ("spotsim.fault.tolerance.ckpt.freq", 150),

	FT_INSTANCE_FAILURES_EXPECTED("spotsim.fault.tolerance.expected", true),

	FT_METHOD("spotsim.fault.tolerance.method", FaultToleranceMethod.CHKPT),

	LOG_LEVEL("spotsim.log.level", "INFO"),

	PRICING_CHARGEABLE_PERIOD("spotsim.pricing.min.period", 3600),

	MODEL_PARAM_DOWNEY_JOBS_FILE(
			"spotsim.model.param.downey.job",
			"resources/input/downey/Inspiral_1000_downey_Input.workload.deepaknew"),

	PRICING_COST_FORECASTING_METHOD("spotsim.sch.cost.forecasting.method",
			PriceForecastingMethod.CURRENT),

	PRICING_MINMAX_FORECASTING_METHOD("spotsim.sch.minmax.forecasting.method",
			PriceForecastingMethod.MM),

	PRICING_HISTORY_MAP_DIR("spotsim.pricing.map.dir", "resources/input/pricesMap/"),

	PRICING_INTER_PRICE_DISTR("spotsim.pricing.interprice.distr",
			TraceDistribution.EXPONENTIAL),

	PRICING_INTER_PRICE_DISTR_FILE("spotsim.pricing.inter.price.param.file",
			"resources/input/deepakfiles/inter_dist_exp_us_west_deepak_v1.txt"),

	PRICING_PRICE_DISTR("spotsim.pricing.price.distr",
			TraceDistribution.MIXTURE_OF_GAUSSIANS),

	PRICING_PRICE_DISTR_FILE("spotsim.pricing.price.param.file",
			"resources/input/deepakfiles/price_dist_us_west_adel.txt"),

	PRICING_PRICE_MU_MULT("spotsim.pricing.model.price.mu.mult", 1D),

	PRICING_PRICE_SIGMA_MULT("spotsim.pricing.model.price.sigma.mult", 1D),

	PRICING_TIME_MU_MULT("spotsim.pricing.model.time.mu.mult", 1D),

	PRICING_TIME_SIGMA_MULT("spotsim.pricing.model.time.sigma.mult", 1D),

	PRICING_TRACE_GEN("spotsim.pricing.gen", PriceTraceGen.HISTORY),
	
	PRICING_TRACE_HISTORY_MOD("spotsim.pricing.gen.mod", true),
	
	PRICING_TRACE_HISTORY_MOD_FACTOR("spotsim.pricing.gen.mod.facotr", 0.6),

	REPORT_DETAILED("spotsim.report.detailed", false),

	REPORT_REDO_EXISTING_RESULTS("spotsim.report.redo", true),

	RNG_SEED("spotsim.rng.seed", 4L),

	SCHED_BIDDING_STRAT("spotsim.sch.bidding.strategy",
			BiddingStrategy.ONDEMAND),

	SCHED_INTERVAL("spotsim.sch.interval", 10),

	SCHED_N_DAYS_FORECASTING("spotsim.sch.forecasting.ndays", 7),

	SCHED_PAST_RUNTIMES("spotsim.sch.past.runtimes", 2),

	SCHED_PESSIMIST_FACTOR("spotsim.sch.pessimism.factor", 4),

	SCHED_POLICY_CLASS("spotsim.sch.policy.class", WorkflowPolicies.NEW_CONSERVATIVE),

	SCHED_RUNTIME_ESTIMATION("spotsim.sch.runtime.estimation.method",
			RuntimeEstimationMethod.RECENT_AVERAGE),

	SIM_DAYS_OFFSET_TO_LOAD("spotsim.duration.days", 0),

	SIM_DAYS_TO_LOAD("spotsim.duration.days", 2),

	SIM_DURATION_HOURS("spotsim.duration.days", SIM_DAYS_TO_LOAD.asInt() * 24),

	SIM_DURATION_SECONDS("spotsim.duration.seconds",
			SIM_DURATION_HOURS.asInt() * 3600),

	SIM_DURATION_MILIS("spotsim.duration.seconds", SIM_DURATION_SECONDS
			.asLong() * 1000L),

//	HESSAM CHANGED
	SIM_START_TIME("spotsim.duration.start", "2018-01-12 00:00:00"),
	
	SIM_START_TIME_SKIP_DAYS("spotsim.duration.start.skipDyas", 3),

//	HESSAM CHANGED
	SIM_END_TIME("spotsim.duration.end", "2018-04-9 00:00:00"),

	WORKLOAD_FILE("spotsim.workload.file", "LCG.swf"),

	WORKLOAD_JOBS("spotsim.workload.jobs", 10000),

	WORKLOAD_LENGTH_MULTIPLIER("spotsim.workload.length.multiplier", 1),

	FAILURE_THRESHOLD("spotsim.policy.failure", 1.0),

	DATACENTER_BANDWIDTH("datacenter.bandwidth", 200),

	RISK_FACTOR_ALPHA("spotsim.bid.alpha", 0.00005),

	BUFFER_FACTOR_BETA("spotsim.bid.beta", 0.09),

	WORKFLOW_BIDDING_STRATEGY("workflow.bid.strategy",
			"org.cloudbud.cloudsim.workflow.policies.WorkflowBiddingStrategy"),

	CONTENTION_FLAG("spotsim.bid.contention.flag", false),

	CONTENTION_THRESHOLD_FACTOR("spotsim.bid.threshold", 0.97),

	CONTENTION_FACTOR_SIGMA("spotsim.bid.sigma", -0.00003645),

	WORKFLOW_DEADLINE("spotsim.workflow.deadline", 45000),

	WORKFLOW_BUDGET("spotsim.workflow.budget", 55000.9),

	WORKFLOW_LENGTH_MULT("spotsim.workflow.lengthMult", 60),

	//mowsc
	LIBERAL_POLICY_REF_INSTANCE("spotsim.workflow.liberalpolicy.refInstance",
			InstanceType.M32XLARGE),

	WF_INPUT_FILE_MODE("spotsim.workflow.inputFileMode", true),

	WF_NUMBER_OF_EXECUTIONS("spotsim.workflow.numExecutions", 1),

	PERFORMANCE_VARIATION_FLAG("spotsim.workflow.performance.variation.flag",
			true),

	PERFORMANCE_VARIATION_SEED("spotsim.performance.variation.seed", 10L),

	PERFORMANCE_VARIATION_STD("spotsim.performance.variation.std", 0.10D),

	PERFORMANCE_VARIATOR("spotsim.performance.variator",
			PerformanceVariatorEnum.UNIFORM_PERF_VAR),
	
	CHKPT_VOLATILITY("spotsim.chekpoint.volatility", 0.10D),
	//False - then switch works, true- no switch
	SWITCH_FLAG("spotsim.workflow.switch.flag",
					false),
					
	WEIBULL_ALPHA("workflow.failure.weibull.alpha",0.2D),
	
	WEIBULL_BETA("workflow.failure.weibull.beta",22.37D),
	
	WEIBULL_DELAY_FAILURE("workflow.failure.weibull.delay",900),
	
	TASKDUP_NUM("workflow.taskdup.policy.dupnum", 1),

//	HESSAM CHANGED
	WORKFLOW_FILE_DAG(
			"spotsim.workflow.dag1",
			"resources/input/inputDAGfiles/Inspiral_30.xml"),

//	HESSAM CHANGED
	WORKFLOW_OUTPUT_DIRECTORY(
			"spotsim.workflow.output.directory",
			"resources/output/workflowOutput/"),

	WORKFLOW_OUTPUT_FILE_EXTENSION("workflow.output.file.extension","300R"),

//	HESSAM CHANGED
	WORKFLOW_INPUT_FILE(
			"spotsim.workflow.input.file",
			"resources/input//workflowInput/test.csv");

	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private final String name;

	private final Class<?> type;

	private Object value;

	private SimProperties(String name) {
		this(name, String.class);
	}

	private SimProperties(String name, Class<?> type) {
		this.name = name;
		this.type = type;
	}

	private SimProperties(String name, Object defaultValue) {
		this.name = name;
		this.value = defaultValue;
		final Class<?> class1 = defaultValue.getClass();
		this.type = !class1.isEnum() && class1.getSuperclass().isEnum() ? class1
				.getSuperclass() : class1;
	}

//	HESSAM ADDED
	public Object getValue() {
		return value;
	}

	public static String getDigest() {

		StringBuilder b = new StringBuilder();
		for (SimProperties prop : values()) {
			b.append(prop.name).append(prop.value);
		}
		try {
			MessageDigest instance = MessageDigest.getInstance("MD5");
			instance.update(b.toString().getBytes());
			return toHex(instance.digest());
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	private static String toHex(byte[] a) {
		StringBuilder sb = new StringBuilder(a.length * 2);
		for (byte element : a) {
			sb.append(Integer.toHexString(0xFF & element));
		}
		return sb.toString();
	}

	@Override
	public boolean asBoolean() {
		checkNull();
		if (this.type.equals(Boolean.class)) {
			return ((Boolean) this.value).booleanValue();
		} else if (this.type.equals(String.class)) {
			return Boolean.parseBoolean((String) this.value);
		}
		throw castEx(Boolean.class);
	}

	@Override
	public GregorianCalendar asDate() {
		checkNull();
		if (this.value instanceof GregorianCalendar) {
			return (GregorianCalendar) this.value;
		} else if (this.type.equals(String.class)) {
			try {
				final GregorianCalendar c = new GregorianCalendar();
				c.setTime(new SimpleDateFormat(DATE_FORMAT)
						.parse((String) this.value));
				return c;
			} catch (ParseException e) {
				throw new IllegalArgumentException(e);
			}
		}
		throw castEx(GregorianCalendar.class);
	}

	@Override
	public double asDouble() {
		checkNull();
		if (isNumber()) {
			return ((Number) this.value).doubleValue();
		} else if (this.type.equals(String.class)) {
			return Double.parseDouble((String) this.value);
		}
		throw castEx(Double.class);
	}

	@Override
	public Enum<?> asEnum() {
		checkNull();
		if (this.type.isEnum()) {
			return (Enum<?>) this.value;
		}
		throw castEx(Enum.class);
	}

	@Override
	public <E extends Enum<E>> E asEnum(Class<E> enumType) {
		checkNull();
		if (this.type.isEnum() && enumType.equals(this.type)) {
			return (E) this.value;
		}
		throw castEx(enumType);
	}

	@Override
	public int asInt() {
		checkNull();
		if (isNumber()) {
			return ((Number) this.value).intValue();
		} else if (this.type.equals(String.class)) {
			return Integer.parseInt((String) this.value);
		}
		throw castEx(Integer.class);
	}

	@Override
	public long asLong() {
		checkNull();
		if (isNumber()) {
			return ((Number) this.value).longValue();
		} else if (this.type.equals(String.class)) {
			return Long.parseLong((String) this.value);
		}
		throw castEx(Long.class);
	}

	@Override
	public Object asObject() {
		checkNull();
		return this.value;
	}

	@Override
	public <T> T asType(Class<T> clazz) {
		checkNull();
		if (clazz.isAssignableFrom(this.type)) {
			return (T) this.value;
		}
		throw castEx(clazz);
	}

	@Override
	public String asString() {
		checkNull();
		return this.value.toString();
	}

	@Override
	public String numberAsString(String pattern) {
		checkNull();
		if (!isNumber()) {
			throw castEx(Number.class);
		}
		return new DecimalFormat(pattern).format(((Number) this.value)
				.doubleValue());
	}

	public String getName() {
		return this.name;
	}

	@Override
	public void set(Object v) {
		if (this.type.equals(String.class)) {
			this.value = v.toString();
		} else if (!this.type.isAssignableFrom(v.getClass())) {
			throw castEx(v.getClass());
		}
		this.value = v;
	}

	@Override
	public void read(String property) {
		if (property != null) {
			if (this.type.equals(Boolean.class)
					&& (property.equalsIgnoreCase("false") || property
							.equalsIgnoreCase("true"))) {
				this.value = Boolean.valueOf(property);
			} else if (this.type.equals(Integer.class)) {
				this.value = Integer.valueOf(property);
			} else if (this.type.equals(Long.class)) {
				this.value = Long.valueOf(property);
			} else if (this.type.equals(Double.class)) {
				this.value = Double.valueOf(property);
			} else if (this.type.equals(String.class)) {
				this.value = property;
			} else if (this.type.isEnum()) {
				Enum<?>[] e = (Enum<?>[]) this.type.getEnumConstants();
				if (e.length > 0) {
					this.value = Enum.valueOf(e[0].getDeclaringClass(),
							property);
				}
			} else {
				throw castEx(String.class);
			}
		}
	}

	private ClassCastException castEx(Class<?> casting) {
		return new ClassCastException("Property " + this.name + " is of type "
				+ this.type + ", trying to cast to " + casting);
	}

	private void checkNull() {
		if (this.value == null) {
			throw new NullPointerException("Property " + this.name
					+ " is not set");
		}
	}

	@Override
	public boolean isNumber() {
		return this.value instanceof Number;
	}

	@Override
	public String toString() {
		return getName() + '=' + asString();
	}

	public static void load(File propertiesFile) throws FileNotFoundException,
			IOException {
		Properties props = new Properties();
		props.load(new BufferedReader(new FileReader(propertiesFile)));
		load(props);
	}

	public static void load(Properties props) {
		load((Map) props);
	}

	public static void load(Map<String, String> props) {
		for (SimProperties prop : values()) {
			prop.read(props.get(prop.getName()));
		}
	}

	public static String allToString() {

		StringBuilder b = new StringBuilder();

		SimProperties[] values = values();
		for (SimProperties prop : values) {
			b.append(prop.toString()).append(System.lineSeparator());
		}

		return b.toString();
	}
}
