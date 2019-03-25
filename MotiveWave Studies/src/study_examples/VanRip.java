package study_examples;

import com.motivewave.platform.sdk.common.BarSize;
import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Enums.MAMethod;
import com.motivewave.platform.sdk.common.GuideInfo;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.LineInfo;
import com.motivewave.platform.sdk.common.MarkerInfo;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.BarDescriptor;
import com.motivewave.platform.sdk.common.desc.BarSizeDescriptor;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.EnabledDependency;
import com.motivewave.platform.sdk.common.desc.GuideDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.DoubleDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SettingGroup;
import com.motivewave.platform.sdk.common.desc.SettingTab;
import com.motivewave.platform.sdk.common.desc.SettingsDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.Plot;
import com.motivewave.platform.sdk.study.RuntimeDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;
import com.motivewave.platform.sdk.common.X11Colors;


/** Combines a KAMA, BBands SMI and RSI into one study. */
/** All indicators are modified specifically to work together */


@StudyHeader(
    namespace="com.Rsquared", 
    id="RH_Master_Strat", 
    rb="study_examples.nls.strings", // locale specific strings are loaded from here
    name="TITLE_COMP_SAMPLE",
    desc="DESC_COMP_SAMPLE",
    menu="W. VanRip",
    overlay=false,
    signals=true)
public class VanRip extends com.motivewave.platform.sdk.study.Study //VanRip class
{
	enum Values { MA, MTF1, MTF2, MTF3, MTF4, SIGNAL, HIST, RSI, UP, DOWN,
				// Values for calculating the SMI
				D1, HL1, D_MA1, HL_MA1, 
				D2, HL2, D_MA2, HL_MA2, 
				D3, HL3, D_MA3, HL_MA3, 
				D4, HL4, D_MA4, HL_MA4, };
				
				
  enum Signals { CROSS_ABOVE, CROSS_BELOW, RSI_TOP, RSI_BOTTOM };

  
  /* strings for General Setting, KAMA and SMA */
  final static String MTF_MULTIPLIER = "mtfMult"; //look for 12x the short timeframe
  
  final static String MA_PERIOD = "maPeriod";
  final static String MA_INPUT = "maInput";
  final static String MA_METHOD = "maMethod";
  final static String MA_PATH = "maPath";
  final static String MA_INDICATOR = "maIndicator";
  
  final static String KAMA_PERIOD = "kaPeriod";
  final static String KAMA_INPUT = "kaInput";
  final static String KAMA_METHOD = "kaMethod";
  final static String KAMA_FAST = "kaFast";
  final static String KAMA_SLOW = "kaSlow";
  
  
  /* strings for BBands */
  final static String BB_INPUT = "BBInput";
  final static String BB_METHOD = "BBMethod";
  final static String BB_PERIOD = "BBPeriod";
  final static String BB_PATH = "BBPath";
  final static String BB_INDICATOR = "BBIndicator";
  final static String BB_STD_1 = "BBstd1";  //Std Devs
  final static String BB_STD_2 = "BBstd2";
  final static String BB_STD_3 = "BBstd3";
  final static String BB_STD_4 = "BBstd4";
  
  final static String BB_STD_L1 = "BBStdL1"; //Lower Bands
  final static String BB_STD_L2 = "BBStdL2";
  final static String BB_STD_L3 = "BBStdL3";
  final static String BB_STD_L4 = "BBStdL4";
  final static String BB_STD_U1 = "BBStdU1"; //Upper Bands
  final static String BB_STD_U2 = "BBStdU2";
  final static String BB_STD_U3 = "BBStdU3";
  final static String BB_STD_U4 = "BBStdU4";
  
  final static String BB_INDICATOR_L1 = "BBIndicatorL1";
  final static String BB_INDICATOR_L2 = "BBIndicatorL2";
  final static String BB_INDICATOR_L3 = "BBIndicatorL3";
  final static String BB_INDICATOR_L4 = "BBIndicatorL4";
  
  
  /* *** strings for SMI *** */
  
  final static String SMI_METHOD = "smiMethod";
  final static String SMI_INPUT = "smiInput";
  final static String SMI_SMOOTH = "smiSmooth";
  final static String SMI_SIGNAL = "smiSignal";
  
  final static String SMI_HL_INC = "smiHLinc"; //Current timeframe increments
  final static String SMI_MA_INC = "smiMAinc";
  final static String SMI_HL_MTF_INC = "smiHLmtfinc"; //MTF increments
  final static String SMI_MA_MTF_INC = "smiMAmtfinc";
  
  final static String SMI_HL1 = "smiHL1"; //Current timeframe settings
  final static String SMI_MA1 = "smiMA1";
  final static String SMI_HL2 = "smiHL2";
  final static String SMI_MA2 = "smiMA2";
  final static String SMI_HL3 = "smiHL3";
  final static String SMI_MA3 = "smiMA3";
  final static String SMI_HL4 = "smiHL4";
  final static String SMI_MA4 = "smiMA4";
  final static String SMI_HL5 = "smiHL5";
  final static String SMI_MA5 = "smiMA5";
  final static String SMI_HL6 = "smiHL6";
  final static String SMI_MA6 = "smiMA6";
  final static String SMI_HL7 = "smiHL7";
  final static String SMI_MA7 = "smiMA7";
  final static String SMI_HL8 = "smiHL8";
  final static String SMI_MA8 = "smiMA8";
  final static String SMI_HL9 = "smiHL9";
  final static String SMI_MA9 = "smiMA9";
  final static String SMI_HL10 = "smiHL10";
  final static String SMI_MA10 = "smiMA10";
  final static String SMI_HL11 = "smiHL11";
  final static String SMI_MA11 = "smiMA11";
  final static String SMI_HL12 = "smiHL12";
  final static String SMI_MA12 = "smiMA12";
  final static String SMI_HL13 = "smiHL13";
  final static String SMI_MA13 = "smiMA13";
  
  final static String SMI_HL1_MTF = "smiHL1mtf"; //SMI MTF settings
  final static String SMI_MA1_MTF = "smiMA1mtf";
  final static String SMI_HL2_MTF = "smiHL2mtf";
  final static String SMI_MA2_MTF = "smiMA2mtf";
  final static String SMI_HL3_MTF = "smiHL3mtf";
  final static String SMI_MA3_MTF = "smiMA3mtf";
  final static String SMI_HL4_MTF = "smiHL4mtf";
  final static String SMI_MA4_MTF = "smiMA4mtf";
  
  final static String SMI_LINE1 = "smiL1";  //SMI Line Settings
  final static String SMI_LINE2 = "smiL2";
  final static String SMI_LINE3 = "smiL3";
  final static String SMI_LINE4 = "smiL4";
  final static String SMI_LINE5 = "smiL5";
  final static String SMI_LINE6 = "smiL6";
  final static String SMI_LINE7 = "smiL7";
  final static String SMI_LINE8 = "smiL8";
  final static String SMI_LINE9 = "smiL9";
  final static String SMI_LINE10= "smiL10";
  final static String SMI_LINE11= "smiL11";
  final static String SMI_LINE12= "smiL12";
  final static String SMI_LINE13= "smiL13";
  
  final static String SMI_LINE1_MTF = "smiL1mtf";
  final static String SMI_LINE2_MTF = "smiL2mtf";
  final static String SMI_LINE3_MTF = "smiL3mtf";
  final static String SMI_LINE4_MTF = "smiL4mtf";
  
  
  
  /* ***  strings for RSI *** */
  
  final static String RSI_PERIOD = "rsiPeriod";
  final static String RSI_INPUT = "rsiInput";
  final static String RSI_METHOD = "rsiMethod";
  
  final static String RSI_BB_STD = "rsiBBstd";  //BB on RSI
  final static String RSI_BB_L1 = "rsiBBL1";
  final static String RSI_BB_U1 = "rsiBBU1";

  final static String RSI_LINE = "rsiLine";
  final static String RSI_IND = "rsiInd";

  final static String RSI_PLOT = "RSIPlot"; //for plot under SMI

  @Override
  public void initialize(Defaults defaults)
  {
	  
	SettingsDescriptor sd = new SettingsDescriptor();
    setSettingsDescriptor(sd);
    
    SettingTab tab = new SettingTab("Price Chart");
    sd.addTab(tab);
    
    /* Simple Moving Average */
    SettingGroup inputs = new SettingGroup("Moving Average");
    	
    inputs.addRow(new IntegerDescriptor(MA_PERIOD, "Period", 50, 1, 300, 1),
			   new MAMethodDescriptor(MA_METHOD, "Method", Enums.MAMethod.EMA),
			   new InputDescriptor(MA_INPUT, "Input", Enums.BarInput.CLOSE));

    inputs.addRow(new PathDescriptor(MA_PATH, get("LBL_LINE"), null, 1.0f, null, true, true, true));
    inputs.addRow(new IndicatorDescriptor(MA_INDICATOR,"Line Label", null, null, false, true, true));
        
    tab.addGroup(inputs);
        
        
    
    /* Bollinger Bands */
    SettingGroup inputs2 = new SettingGroup("Bollinger Band Inputs");
    inputs2.addRow(new IntegerDescriptor(BB_PERIOD, "Period", 50, 1, 300, 1),
    			   new MAMethodDescriptor(BB_METHOD, "Method", Enums.MAMethod.EMA),
    			   new InputDescriptor(BB_INPUT, "Input", Enums.BarInput.CLOSE));
    tab.addGroup(inputs2);
    
    SettingGroup inputs3 = new SettingGroup("Standard Deviations");
    inputs3.addRow(new DoubleDescriptor(BB_STD_1, "Std Dev 1", 2.0, .1, 3, .1));  //Std Dev
    inputs3.addRow(new DoubleDescriptor(BB_STD_2, "Std Dev 2", 2.2, .1, 3, .1));
    inputs3.addRow(new DoubleDescriptor(BB_STD_3, "Std Dev 3", 2.4, .1, 3, .1));
    inputs3.addRow(new DoubleDescriptor(BB_STD_4, "Std Dev 4", 2.6, .1, 3, .1));
    tab.addGroup(inputs3);
    
    /* Bollinger Band Colors */
    SettingGroup colors = new SettingGroup("Line Colors");
    colors.addRow(new PathDescriptor(BB_PATH, "Middle", null, 1.0f, null, true, true, true));
    colors.addRow(new PathDescriptor(BB_STD_L1, get("Std Dev 1"), null, 1.0f, null, true, true, true));
    colors.addRow(new PathDescriptor(BB_STD_L2, get("Std Dev 2"), null, 1.0f, null, true, true, true));
    colors.addRow(new PathDescriptor(BB_STD_L3, get("Std Dev 3"), null, 1.0f, null, true, true, true));   
    colors.addRow(new PathDescriptor(BB_STD_L4, get("Std Dev 4"), null, 1.0f, null, true, true, true));
    tab.addGroup(colors);
    
    SettingGroup colors2 = new SettingGroup("Line Labels");
    colors2.addRow(new IndicatorDescriptor(BB_INDICATOR, "Middle Label", null, null, false, false, true));
	colors2.addRow(new IndicatorDescriptor(BB_INDICATOR_L1, "Std Dev 1 Label", null, null, false, false, true));
	colors2.addRow(new IndicatorDescriptor(BB_INDICATOR_L2, "Std Dev 2 Label", null, null, false, false, true));
	colors2.addRow(new IndicatorDescriptor(BB_INDICATOR_L3, "Std Dev 3 Label", null, null, false, false, true));    
	colors2.addRow(new IndicatorDescriptor(BB_INDICATOR_L4, "Std Dev 4 Label", null, null, false, false, true));
    tab.addGroup(colors2);

    /* Stochastic Momentum Index */

    tab = new SettingTab("SMI");
    sd.addTab(tab);
    
    inputs = new SettingGroup(get("LBL_INPUTS"));
    
	inputs.addRow(new MAMethodDescriptor(SMI_METHOD, get("LBL_METHOD"), Enums.MAMethod.EMA),
			      new InputDescriptor(SMI_INPUT, get("LBL_INPUT"), Enums.BarInput.CLOSE));

	inputs.addRow(new IntegerDescriptor(SMI_SIGNAL, "Signal Period", 1, 1, 300, 1),
		  	  new IntegerDescriptor(SMI_SMOOTH, "Smooth Period", 2, 1, 300, 1));
   
	tab.addGroup(inputs);
    
    inputs = new SettingGroup("Inputs");
    
    inputs.addRow(new IntegerDescriptor(SMI_HL1, "H/L Period", 3, 1, 300, 1),
				  new IntegerDescriptor(SMI_HL_INC, "Increment by", 1, 1, 300, 1));
	inputs.addRow(new IntegerDescriptor(SMI_MA1, "MA Period", 5, 1, 300, 1),
			  	  new IntegerDescriptor(SMI_MA_INC, "Increment by", 2, 1, 300, 1));
	tab.addGroup(inputs);
	
	SettingGroup colors4 = new SettingGroup("FAST SMI Line Colors");	
	colors4.addRow(new PathDescriptor(SMI_LINE1, "Line 1", null, 1.0f, null, true, true, true));
	colors4.addRow(new PathDescriptor(SMI_LINE2, "Line 2", null, 1.0f, null, true, true, true));
	colors4.addRow(new PathDescriptor(SMI_LINE3, "Line 3", null, 1.0f, null, true, true, true));
	colors4.addRow(new PathDescriptor(SMI_LINE4, "Line 4", null, 1.0f, null, true, true, true));
	colors4.addRow(new PathDescriptor(SMI_LINE5, "Line 5", null, 1.0f, null, true, true, true));
	colors4.addRow(new PathDescriptor(SMI_LINE6, "Line 6", null, 1.0f, null, true, true, true));
	colors4.addRow(new PathDescriptor(SMI_LINE7, "Line 7", null, 1.0f, null, true, true, true));
	colors4.addRow(new PathDescriptor(SMI_LINE8, "Line 8", null, 1.0f, null, true, true, true));
	colors4.addRow(new PathDescriptor(SMI_LINE9, "Line 9", null, 1.0f, null, true, true, true));
	colors4.addRow(new PathDescriptor(SMI_LINE10, "Line 10", null, 1.0f, null, true, true, true));
	colors4.addRow(new PathDescriptor(SMI_LINE11, "Line 11", null, 1.0f, null, true, true, true));
	colors4.addRow(new PathDescriptor(SMI_LINE12, "Line 12", null, 1.0f, null, true, true, true));
	colors4.addRow(new PathDescriptor(SMI_LINE13, "Line 13", null, 1.0f, null, true, true, true));
	tab.addGroup(colors4);
	
	/*  SMI MTF Input Tab   */  /* Stochastic Momentum Index */

    tab = new SettingTab("MTF");
    sd.addTab(tab);
    
	inputs = new SettingGroup("Inputs");
    
	inputs.addRow(new IntegerDescriptor(MTF_MULTIPLIER, "Timeframe Multiplier", 12, 1, 50, 1));
	
	inputs.addRow(new IntegerDescriptor(SMI_HL1_MTF, "H/L Period", 14, 1, 300, 1),
				  new IntegerDescriptor(SMI_HL_MTF_INC, "Increment by", 1, 1, 300, 1));
	inputs.addRow(new IntegerDescriptor(SMI_MA1_MTF, "MA Period", 27, 1, 300, 1),
			  	  new IntegerDescriptor(SMI_MA_MTF_INC, "Increment by", 2, 1, 300, 1));
	tab.addGroup(inputs);
	
	SettingGroup colors3 = new SettingGroup("MTF SMI Line Colors");	
	colors3.addRow(new PathDescriptor(SMI_LINE1_MTF, "Line 1", null, 1.0f, null, true, true, true));
	colors3.addRow(new PathDescriptor(SMI_LINE2_MTF, "Line 2", null, 1.0f, null, true, true, true));
	colors3.addRow(new PathDescriptor(SMI_LINE3_MTF, "Line 3", null, 1.0f, null, true, true, true));
	colors3.addRow(new PathDescriptor(SMI_LINE4_MTF, "Line 4", null, 1.0f, null, true, true, true));

	tab.addGroup(colors3);
	
    /*  RSI Input Tab   */ 
    
    tab = new SettingTab(get("TAB_RSI"));
    sd.addTab(tab);

    inputs = new SettingGroup("Inputs");
    inputs.addRow(new IntegerDescriptor(RSI_PERIOD, get("LBL_PERIOD"), 14, 1, 9999, 1));
    inputs.addRow(new MAMethodDescriptor(RSI_METHOD, get("LBL_METHOD"), Enums.MAMethod.EMA));
    inputs.addRow(new InputDescriptor(RSI_INPUT, get("LBL_INPUT"), Enums.BarInput.CLOSE));
    tab.addGroup(inputs);
    
    colors = new SettingGroup("Lines");
    colors.addRow(new PathDescriptor(RSI_LINE, get("LBL_RSI_LINE"), defaults.getLineColor(), 1.0f, null));
    colors.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("LBL_TOP_FILL"), Inputs.TOP_GUIDE, RSI_LINE, Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));
    colors.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("LBL_BOTTOM_FILL"), Inputs.BOTTOM_GUIDE, RSI_LINE, Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));
    colors.addRow(new IndicatorDescriptor(RSI_IND, "RSI Label", null, null, false, true, true));
    tab.addGroup(colors);

    SettingGroup guides = new SettingGroup(get("LBL_GUIDES"));
    guides.addRow(new GuideDescriptor(Inputs.TOP_GUIDE, get("LBL_TOP_GUIDE"), 70, 1, 100, 1, true));
    GuideDescriptor mg = new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("LBL_MIDDLE_GUIDE"), 50, 1, 100, 1, true);
    mg.setDash(new float[] {3, 3});
    guides.addRow(mg);
    guides.addRow(new GuideDescriptor(Inputs.BOTTOM_GUIDE, get("LBL_BOTTOM_GUIDE"), 30, 1, 100, 1, true));
    tab.addGroup(guides);
    
    inputs2 = new SettingGroup("Standard Deviation Bands");
    inputs2.addRow(new DoubleDescriptor(RSI_BB_STD, "Std Dev", .118, .001, 3, .001));
	inputs2.addRow(new PathDescriptor(RSI_BB_U1, "Upper Band", defaults.getYellow(), 1.0f, null, true, true, true));
	inputs2.addRow(new PathDescriptor(RSI_BB_L1, "Lower Band", defaults.getYellow(), 1.0f, null, true, true, true));
    tab.addGroup(inputs2);
    
    /*                                                    */
    RuntimeDescriptor desc = new RuntimeDescriptor();
    setRuntimeDescriptor(desc);

    desc.exportValue(new ValueDescriptor(Values.MA, "MA", new String[] {MA_INPUT, MA_PERIOD, Inputs.SHIFT, Inputs.BARSIZE}));
    desc.exportValue(new ValueDescriptor(Values.MTF, "MTF", new String[] {SMI_INPUT, MTF_MULTIPLIER}));
    desc.exportValue(new ValueDescriptor(Values.RSI, get("LBL_RSI"), new String[] {RSI_INPUT, RSI_PERIOD}));
    //desc.exportValue(new ValueDescriptor(Values.MACD, get("LBL_MACD"), new String[] {MACD_INPUT, MACD_METHOD, MACD_PERIOD1, MACD_PERIOD2}));
    //desc.exportValue(new ValueDescriptor(Values.SIGNAL, get("LBL_MACD_SIGNAL"), new String[] {Inputs.SIGNAL_PERIOD}));
    //desc.exportValue(new ValueDescriptor(Values.HIST, get("LBL_MACD_HIST"), new String[] {MACD_PERIOD1, MACD_PERIOD2, Inputs.SIGNAL_PERIOD}));

    desc.declareSignal(Signals.CROSS_ABOVE, get("LBL_CROSS_ABOVE_SIGNAL"));
    desc.declareSignal(Signals.CROSS_BELOW, get("LBL_CROSS_BELOW_SIGNAL"));
    desc.declareSignal(Signals.RSI_TOP, get("RSI_TOP"));
    desc.declareSignal(Signals.RSI_BOTTOM, get("RSI_BOTTOM"));
    
    // Price plot (moving average)
    desc.getPricePlot().setLabelSettings(MA_INPUT, MA_PERIOD, Inputs.SHIFT, Inputs.BARSIZE);
    desc.getPricePlot().setLabelPrefix("MA");
    desc.getPricePlot().declarePath(Values.MA, Inputs.PATH);
    desc.getPricePlot().declareIndicator(Values.MA, Inputs.IND);
    
    //TEST MTF PLOT
    
    desc.getPricePlot().setLabelSettings(SMI_INPUT, MTF_MULTIPLIER);
    desc.getPricePlot().setLabelPrefix("MTF");
    desc.getPricePlot().declarePath(Values.MTF, SMI_LINE1_MTF);
    //desc.getPricePlot().declareIndicator(Values.MA, Inputs.IND)
    
    
    // This tells MotiveWave that the MA values come from the data series defined by "BARSIZE"
    desc.setValueSeries(Values.MTF, SMI_INPUT);

    /* Default Plot (MACD)
    desc.setLabelSettings(MACD_INPUT, MACD_METHOD, MACD_PERIOD1, MACD_PERIOD2, Inputs.SIGNAL_PERIOD);
    desc.setLabelPrefix("MACD");
    desc.setTabName("MACD");
    desc.declarePath(Values.MACD, MACD_LINE);
    desc.declarePath(Values.SIGNAL, Inputs.SIGNAL_PATH);
    desc.declareBars(Values.HIST, Inputs.BAR);
    desc.declareIndicator(Values.MACD, MACD_IND);
    desc.declareIndicator(Values.SIGNAL, Inputs.SIGNAL_IND);
    desc.declareIndicator(Values.HIST, HIST_IND);
    desc.setRangeKeys(Values.MACD, Values.SIGNAL, Values.HIST);
    desc.addHorizontalLine(new LineInfo(0, null, 1.0f, new float[] {3,3}));
    */
    
    // RSI Plot
    Plot rsiPlot = new Plot();
    desc.addPlot(RSI_PLOT, rsiPlot);
    
    rsiPlot.setLabelSettings(RSI_INPUT, RSI_PERIOD);
    rsiPlot.setLabelPrefix("RSI");
    rsiPlot.setTabName("RSI");
    rsiPlot.declarePath(Values.RSI, RSI_LINE);
    rsiPlot.declareIndicator(Values.RSI, RSI_IND);
    rsiPlot.declareGuide(Inputs.TOP_GUIDE);
    rsiPlot.declareGuide(Inputs.MIDDLE_GUIDE);
    rsiPlot.declareGuide(Inputs.BOTTOM_GUIDE);
    rsiPlot.setMaxBottomValue(15);
    rsiPlot.setMinTopValue(85);
    rsiPlot.setRangeKeys(Values.RSI);
    rsiPlot.setMinTick(0.1);
  }

  // Since the Stochastic (MTF SMI) is plotted on a different data series, we need to override this method 
  // and manually compute the MA for the secondary data series.
  @Override
  protected void calculateValues(DataContext ctx)
  {
	//Common MTF Inputs
	int mtfPeriod = getSettings().getInteger(MTF_MULTIPLIER);
    Object mtfInput = getSettings().getInput(SMI_INPUT, Enums.BarInput.CLOSE);
    
    int mtfSmooth = getSettings().getInteger(SMI_SMOOTH);
    int mtfSignal = getSettings().getInteger(SMI_SIGNAL);
    int mtfHLinc = getSettings().getInteger(SMI_HL_MTF_INC);
    int mtfMAinc = getSettings().getInteger(SMI_MA_MTF_INC);
    
	int mtfHL = getSettings().getInteger(SMI_HL1_MTF);
	int mtfMA = getSettings().getInteger(SMI_MA1_MTF);
    
    /* MTF Bar sizing */
    BarSize barSize = ctx.getChartBarSize(); //Gets the barsize off the chart
    int mtfbarint = barSize.getInterval();  //Gets the interval of the chart
    int barSizeint = mtfbarint * mtfPeriod; //Multiply the interval by the mtf multiplier
       
    //Calculates a longer period interval based upon the mtfPeriod
    BarSize barSizeNew = barSize.getBarSize(barSize.getType(), barSizeint); 
    //Assembes the longer period timeframe series
    DataSeries series2 = ctx.getDataSeries(barSizeNew);

    String valStringOut;  //variables for the return results
    String valStringD;
    String valStringHL;
    String valStringD_MA;
    String valStringHL_MA;
    int hlPeriodmtf;
    int maPeriodmtf;
    
    StudyHeader header = getHeader();
    boolean updates = getSettings().isBarUpdates() || (header != null && header.requiresBarUpdates());

    // Calculates Moving Average for the Secondary Data Series
    for(int i = 1; i < series2.size(); i++) {
      if (series2.isComplete(i)) continue;
      if (!updates && !series2.isBarComplete(i)) continue;
      //Double sma = series2.ma(MAMethod.SMA, i, mtfPeriod, mtfInput);
      
      //insert smi logic
      for(int j = 1; j <= 4; j++)  {
    	  
      switch (j) {
      case 1:
    	  valStringOut 	 = "Values.MTF1"; //D1, HL1, D_MA1, HL_MA1 mtfHLinc
    	  valStringD   	 = "Values.D1";
    	  valStringHL  	 = "Values.HL1";
    	  valStringD_MA	 = "Values.D_MA1";
    	  valStringHL_MA = "Values.HL_MA1";
    	  hlPeriodmtf    = mtfHL; //Base HL
    	  maPeriodMA     = mtfMA; //Base MA
    	  break;
      case 2:
    	  valStringOut   = "Values.MTF2";
    	  valStringD   	 = "Values.D2";
    	  valStringHL  	 = "Values.HL2";
    	  valStringD_MA	 = "Values.D_MA2";
    	  valStringHL_MA = "Values.HL_MA2";
    	  hlPeriodmtf    = mtfHL + mtfHLinc; //Base HL + Increment
    	  maPeriodMA     = mtfMA + mtfMAinc; //Base MA + Increment  	  
    	  break;
      case 3:
    	  valStringOut   = "Values.MTF3";
    	  valStringD   	 = "Values.D3";
    	  valStringHL  	 = "Values.HL3";
    	  valStringD_MA	 = "Values.D_MA3";
    	  valStringHL_MA = "Values.HL_MA3";
    	  hlPeriodmtf    = mtfHL + (mtfHLinc*2); //Base HL + Increment*2
    	  maPeriodMA     = mtfMA + (mtfMAinc*2); //Base MA + Increment*2    	  
    	  break;
      case 4:
    	  valStringOut   = "Values.MTF4";
    	  valStringD   	 = "Values.D4";
    	  valStringHL  	 = "Values.HL4";
    	  valStringD_MA	 = "Values.D_MA4";
    	  valStringHL_MA = "Values.HL_MA4";
    	  hlPeriodmtf    = mtfHL + (mtfHLinc*3); //Base HL + Increment
    	  maPeriodMA     = mtfMA + (mtfMAinc*3); //Base MA + Increment
    	  break;
      default:
    	  break;	  
      }   //end switch
      
      //base HL period is mtfHL
      if (i < mtfHL) return;

      double HH = series2.highest(i, hlPeriodmtf, Enums.BarInput.HIGH);
      double LL = series2.lowest(i, hlPeriodmtf, Enums.BarInput.LOW);
      double M = (HH + LL)/2.0;
      double D = series2.getClose(i) - M;
      
      series.setDouble(i, valStringD, D);
      series.setDouble(i, valStringHL, HH - LL);
      
      int maPeriod = getSettings().getInteger(MA_PERIOD);
      if (index < hlPeriod + maPeriod) return;
      
      Enums.MAMethod method = getSettings().getMAMethod(Inputs.METHOD);
      series.setDouble(index, Values.D_MA, series.ma(method, index, maPeriod, Values.D));
      series.setDouble(index, Values.HL_MA, series.ma(method, index, maPeriod, Values.HL));
      
      int smoothPeriod= getSettings().getInteger(SMOOTH_PERIOD);
      if (index < hlPeriod + maPeriod + smoothPeriod) return;
      
      Double D_SMOOTH = series.ma(method, index, smoothPeriod, Values.D_MA);
      Double HL_SMOOTH = series.ma(method, index, smoothPeriod, Values.HL_MA);
      
      if (D_SMOOTH == null || HL_SMOOTH == null) return;
      double HL2 = HL_SMOOTH/2;
      double SMI = 0;
      if (HL2 != 0) SMI = 100 * (D_SMOOTH/HL2);

      series.setDouble(index, Values.SMI, SMI);

      int signalPeriod= getSettings().getInteger(Inputs.SIGNAL_PERIOD);
      if (index < hlPeriod + maPeriod + smoothPeriod + signalPeriod) return;

      Double signal = series.ma(method, index, signalPeriod, Values.SMI);
      if (signal == null) return;
      series.setDouble(index, Values.SMI_SIGNAL, signal);

      
      
      }  //end j bracket
      
     
      
      
      
      series2.setDouble(i, Values.MTF, sma);
    }

    // Invoke the parent method to run the "calculate" method below for the primary (chart) data series
    super.calculateValues(ctx);
  }
  
  // Computes the values for the MACD and RSI plots.  These plots use the primary (chart) data series.
  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    DataSeries series = ctx.getDataSeries();
    boolean complete=true;
    
    /*
    int macdPeriod1 = getSettings().getInteger(MACD_PERIOD1, 12);
    int macdPeriod2 = getSettings().getInteger(MACD_PERIOD2, 26);
    Enums.MAMethod macdMethod = getSettings().getMAMethod(MACD_METHOD, Enums.MAMethod.EMA);
    Object macdInput = getSettings().getInput(MACD_INPUT, Enums.BarInput.CLOSE);
    if (index >= Util.max(macdPeriod1, macdPeriod2)) {
      Double MA1 = null, MA2 = null;
      MA1 = series.ma(macdMethod, index, macdPeriod1, macdInput);
      MA2 = series.ma(macdMethod, index, macdPeriod2, macdInput);
      
      double MACD = MA1 - MA2; 
      series.setDouble(index, Values.MACD, MACD);

      int signalPeriod = getSettings().getInteger(Inputs.SIGNAL_PERIOD, 9);

      // Calculate moving average of MACD (signal line)
      Double signal = series.ma(getSettings().getMAMethod(Inputs.SIGNAL_METHOD, Enums.MAMethod.SMA), index, signalPeriod, Values.MACD);
      series.setDouble(index, Values.SIGNAL, signal);
    
      if (signal != null) series.setDouble(index, Values.HIST, MACD - signal);

      if (series.isBarComplete(index)) {
        // Check for signal events
        Coordinate c = new Coordinate(series.getStartTime(index), signal);
        if (crossedAbove(series, index, Values.MACD, Values.SIGNAL)) {
          MarkerInfo marker = getSettings().getMarker(Inputs.UP_MARKER);
          String msg = get("SIGNAL_MACD_CROSS_ABOVE", MACD, signal);
          if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
          ctx.signal(index, Signals.CROSS_ABOVE, msg, signal);
        }
        else if (crossedBelow(series, index, Values.MACD, Values.SIGNAL)) {
          MarkerInfo marker = getSettings().getMarker(Inputs.DOWN_MARKER);
          String msg = get("SIGNAL_MACD_CROSS_BELOW", MACD, signal);
          if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
          ctx.signal(index, Signals.CROSS_BELOW, msg, signal);
        }
      }
    }
    else complete=false;
    
    int rsiPeriod = getSettings().getInteger(RSI_PERIOD);
    Object rsiInput = getSettings().getInput(RSI_INPUT);
    if (index < 1) return; // not enough data
    
    double diff = series.getDouble(index, rsiInput) - series.getDouble(index-1, rsiInput);
    double up = 0, down = 0;
    if (diff > 0) up = diff;
    else down = diff;
    
    series.setDouble(index, Values.UP, up);
    series.setDouble(index, Values.DOWN, Math.abs(down));
    
    if (index <= rsiPeriod +1) return;
    
    Enums.MAMethod method = getSettings().getMAMethod(RSI_METHOD);
    double avgUp = series.ma(method, index, rsiPeriod, Values.UP);
    double avgDown = series.ma(method, index, rsiPeriod, Values.DOWN);
    double RS = avgUp / avgDown;
    double RSI = 100.0 - ( 100.0 / (1.0 + RS));

    series.setDouble(index, Values.RSI, RSI);
    
    // Do we need to generate a signal?
    GuideInfo topGuide = getSettings().getGuide(Inputs.TOP_GUIDE);
    GuideInfo bottomGuide = getSettings().getGuide(Inputs.BOTTOM_GUIDE);
    if (crossedAbove(series, index, Values.RSI, topGuide.getValue())) {
      series.setBoolean(index, Signals.RSI_TOP, true);
      ctx.signal(index, Signals.RSI_TOP, get("SIGNAL_RSI_TOP", topGuide.getValue(), round(RSI)), round(RSI));
    }
    else if (crossedBelow(series, index, Values.RSI, bottomGuide.getValue())) {
      series.setBoolean(index, Signals.RSI_BOTTOM, true);
      ctx.signal(index, Signals.RSI_BOTTOM, get("SIGNAL_RSI_BOTTOM", bottomGuide.getValue(), round(RSI)), round(RSI));
    }
    */
    series.setComplete(index, complete);
  }  
}


