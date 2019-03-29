package com.motivewave.platform.study.general;

import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.MarkerInfo;
import com.motivewave.platform.sdk.common.desc.GuideDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SettingGroup;
import com.motivewave.platform.sdk.common.desc.SettingTab;
import com.motivewave.platform.sdk.common.desc.SettingsDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.RuntimeDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Stochastic Momentum Index */
@StudyHeader(
    namespace="com.motivewave", 
    id="SMI", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_SMI",
    label="LBL_SMI",
    desc="DESC_SMI",
    menu="MENU_GENERAL",
    overlay=false,
    studyOverlay=true,
    signals=true,
    helpLink="http://www.motivewave.com/studies/stochastic_momentum_index.htm")
public class SMI extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { SMI, SMI_SIGNAL, // Exported Values
	              // These values are used for calculating averages and smoothed averages
	              D, HL, D_MA, HL_MA };
	              
	enum Signals { CROSS_ABOVE, CROSS_BELOW };
	
  final static String HL_PERIOD = "hlPeriod";
  final static String MA_PERIOD = "maPeriod";
  final static String SMOOTH_PERIOD = "smoothPeriod";

  @Override
  public void initialize(Defaults defaults)
  {
    SettingsDescriptor sd = new SettingsDescriptor();
    SettingTab tab = new SettingTab(get("TAB_GENERAL"));
    sd.addTab(tab);
    setSettingsDescriptor(sd);

    SettingGroup inputs = new SettingGroup(get("LBL_INPUTS"));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("LBL_METHOD"), Enums.MAMethod.EMA));
    inputs.addRow(new IntegerDescriptor(HL_PERIOD, get("LBL_HL_PERIOD"), 2, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(MA_PERIOD, get("LBL_MA_PERIOD"), 8, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(SMOOTH_PERIOD, get("LBL_SMOOTH_PERIOD"), 5, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.SIGNAL_PERIOD, get("LBL_SIGNAL_PERIOD"), 5, 1, 9999, 1));
    tab.addGroup(inputs);
    
    SettingGroup lines = new SettingGroup(get("LBL_LINES"));
    PathDescriptor path = new PathDescriptor(Inputs.PATH, get("LBL_SMI"), defaults.getLineColor(), 1.5f, null, true, false, true);
    path.setSupportsShowAsBars(true);
    lines.addRow(path);
    PathDescriptor signalPath = new PathDescriptor(Inputs.SIGNAL_PATH, get("LBL_SIGNAL_LINE"), defaults.getRed(), 1.0f, null, true, false, true);
    signalPath.setSupportsShowAsBars(true);
    lines.addRow(signalPath);
    lines.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("LBL_UP_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.VERY_SMALL, defaults.getGreen(), defaults.getLineColor(), true, true));
    lines.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("LBL_DOWN_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.VERY_SMALL, defaults.getRed(), defaults.getLineColor(), true, true));

    tab.addGroup(lines);

    tab = new SettingTab(get("TAB_ADVANCED"));
    sd.addTab(tab);
    SettingGroup indicators = new SettingGroup(get("LBL_INDICATORS"));
    indicators.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));
    indicators.addRow(new IndicatorDescriptor(Inputs.SIGNAL_IND, get("LBL_SIGNAL_IND"), defaults.getRed(), null, false, false, true));
    tab.addGroup(indicators);

    SettingGroup guides = new SettingGroup(get("LBL_GUIDES"));
    guides.addRow(new GuideDescriptor(Inputs.TOP_GUIDE, get("LBL_TOP_GUIDE"), 40, -100, 100, 1, true));
    GuideDescriptor mg = new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("LBL_MIDDLE_GUIDE"), 0, -100, 100, 1, true);
    mg.setDash(new float[] {3, 3});
    guides.addRow(mg);
    guides.addRow(new GuideDescriptor(Inputs.BOTTOM_GUIDE, get("LBL_BOTTOM_GUIDE"), -40, -100, 100, 1, true));
    tab.addGroup(guides);

    RuntimeDescriptor desc = new RuntimeDescriptor();
    desc.setLabelSettings(HL_PERIOD, MA_PERIOD, SMOOTH_PERIOD, Inputs.SIGNAL_PERIOD);
    desc.exportValue(new ValueDescriptor(Values.SMI, get("LBL_SMI"), new String[] {HL_PERIOD, MA_PERIOD, SMOOTH_PERIOD}));
    desc.exportValue(new ValueDescriptor(Values.SMI_SIGNAL, get("LBL_SMI_SIGNAL"), new String[] {Inputs.SIGNAL_PERIOD}));
    desc.declarePath(Values.SMI, Inputs.PATH);
    desc.declarePath(Values.SMI_SIGNAL, Inputs.SIGNAL_PATH);
    desc.declareIndicator(Values.SMI, Inputs.IND);
    desc.declareIndicator(Values.SMI_SIGNAL, Inputs.SIGNAL_IND);
    
    desc.declareSignal(Signals.CROSS_ABOVE, get("LBL_CROSS_ABOVE_SIGNAL"));
    desc.declareSignal(Signals.CROSS_BELOW, get("LBL_CROSS_BELOW_SIGNAL"));
    
    desc.setFixedTopValue(100);
    desc.setFixedBottomValue(-100);
    desc.setMinTick(0.1);
    
    setRuntimeDescriptor(desc);
  }

  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    int hlPeriod = getSettings().getInteger(HL_PERIOD);
    if (index < hlPeriod) return;

    DataSeries series = ctx.getDataSeries();
    double HH = series.highest(index, hlPeriod, Enums.BarInput.HIGH);
    double LL = series.lowest(index, hlPeriod, Enums.BarInput.LOW);
    double M = (HH + LL)/2.0;
    double D = series.getClose(index) - M;
    
    series.setDouble(index, Values.D, D);
    series.setDouble(index, Values.HL, HH - LL);
    
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
    
    if (!series.isBarComplete(index)) return;

    // Check for signal events
    Coordinate c = new Coordinate(series.getStartTime(index), signal);
    if (crossedAbove(series, index, Values.SMI, Values.SMI_SIGNAL)) {
      MarkerInfo marker = getSettings().getMarker(Inputs.UP_MARKER);
      String msg = get("SIGNAL_SMI_CROSS_ABOVE", SMI, signal);
      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
      ctx.signal(index, Signals.CROSS_ABOVE, msg, signal);
    }
    else if (crossedBelow(series, index, Values.SMI, Values.SMI_SIGNAL)) {
      MarkerInfo marker = getSettings().getMarker(Inputs.DOWN_MARKER);
      String msg = get("SIGNAL_SMI_CROSS_BELOW", SMI, signal);
      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
      ctx.signal(index, Signals.CROSS_BELOW, msg, signal);
    }

    series.setComplete(index);
  }  
  
}
