package com.motivewave.platform.study.overlay;

import java.awt.Color;

import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.MarkerInfo;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SettingGroup;
import com.motivewave.platform.sdk.common.desc.SettingTab;
import com.motivewave.platform.sdk.common.desc.SettingsDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.RuntimeDescriptor;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Ichimoku Kinko Hyo */
@StudyHeader(
    namespace="com.motivewave", 
    id="ICHIMOKU", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_ICHIMOKU", 
    label="LBL_ICHIMOKU",
    desc="DESC_ICHIMOKU",
    menu="MENU_OVERLAY",
    overlay=true,
    signals=true,
    helpLink="http://www.motivewave.com/studies/ichimoku_kinko_hyo.htm")
public class IchimokuKinkoHyo extends Study 
{
  final static String TS_LINE = "tsLine", KS_LINE = "ksLine", CS_LINE = "csLine", SSA_LINE = "ssaLine", SSB_LINE = "ssbLine";
  final static String TS_IND = "tsInd", KS_IND = "ksInd", CS_IND = "csInd", SSA_IND = "ssaInd", SSB_IND = "ssbInd";

  enum Values { TS, KS, CS, SSA, SSB }
  enum Signals { TENKAN_CROSS_ABOVE_KIJUN, TENKAN_CROSS_BELOW_KIJUN }

  @Override
  public void initialize(Defaults defaults)
  {
    SettingsDescriptor sd = new SettingsDescriptor();
    SettingTab tab = new SettingTab(get("TAB_GENERAL"));
    sd.addTab(tab);
    setSettingsDescriptor(sd);

    SettingGroup inputs = new SettingGroup(get("LBL_INPUTS"));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_TENKAN_SEN"), 9, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD2, get("LBL_KIJUN_SEN"), 26, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD3, get("LBL_SENKOU_SPANB"), 52, 1, 9999, 1));
    tab.addGroup(inputs);
    
    SettingGroup lines = new SettingGroup(get("LBL_LINES"));
    lines.addRow(new PathDescriptor(TS_LINE, get("LBL_TS_LINE"), defaults.getOrange(), 1.0f, null, true, true, true));
    lines.addRow(new PathDescriptor(KS_LINE, get("LBL_KS_LINE"), defaults.getRed(), 1.0f, null, true, true, true));
    lines.addRow(new PathDescriptor(CS_LINE, get("LBL_CS_LINE"), defaults.getBlue(), 1.0f, null, true, true, true));
    lines.addRow(new PathDescriptor(SSA_LINE, get("LBL_SSA_LINE"), defaults.getGreen(), 1.0f, null, true, true, true));
    lines.addRow(new PathDescriptor(SSB_LINE, get("LBL_SSB_LINE"), defaults.getGreen(), 1.0f, null, true, true, true));
    lines.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("LBL_KUMO_UP"), SSA_LINE, SSB_LINE, Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));
    lines.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("LBL_KUMO_DOWN"), SSA_LINE, SSB_LINE, Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));
    tab.addGroup(lines);

    tab = new SettingTab(get("TAB_ADVANCED"));
    sd.addTab(tab);
    
    SettingGroup markers = new SettingGroup(get("LBL_MARKERS"));
    tab.addGroup(markers);
    markers.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("LBL_UP_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getGreen(), defaults.getLineColor(), true, true));
    markers.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("LBL_DOWN_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getRed(), defaults.getLineColor(), true, true));

    SettingGroup indicators = new SettingGroup(get("LBL_INDICATORS"));
    
    indicators.addRow(new IndicatorDescriptor(TS_IND, get("LBL_TS_IND"), defaults.getOrange(), Color.WHITE, false, false, true));
    indicators.addRow(new IndicatorDescriptor(KS_IND, get("LBL_KS_IND"), defaults.getRed(), Color.WHITE, false, false, true));
    indicators.addRow(new IndicatorDescriptor(CS_IND, get("LBL_CS_IND"), defaults.getBlue(), Color.WHITE, false, false, true));
    indicators.addRow(new IndicatorDescriptor(SSA_IND, get("LBL_SSA_IND"), defaults.getGreen(), Color.WHITE, false, false, true));
    indicators.addRow(new IndicatorDescriptor(SSB_IND, get("LBL_SSB_IND"), defaults.getGreen(), Color.WHITE, false, false, true));
    tab.addGroup(indicators);

    RuntimeDescriptor desc = new RuntimeDescriptor();
    desc.setLabelSettings(Inputs.PERIOD, Inputs.PERIOD2, Inputs.PERIOD3);
    
    desc.exportValue(new ValueDescriptor(Values.TS, get("LBL_TENKAN_SEN"), new String[] {Inputs.PERIOD}));
    desc.exportValue(new ValueDescriptor(Values.KS, get("LBL_KIJUN_SEN"), new String[] {Inputs.PERIOD2}));
    desc.exportValue(new ValueDescriptor(Values.CS, get("LBL_CHIKOU_SPAN"), new String[] {Inputs.PERIOD2}));
    desc.exportValue(new ValueDescriptor(Values.SSA, get("LBL_SENKOU_SPANA"), new String[] {Inputs.INPUT, Inputs.PERIOD2}));
    desc.exportValue(new ValueDescriptor(Values.SSB, get("LBL_SENKOU_SPANB"), new String[] {Inputs.INPUT, Inputs.PERIOD2, Inputs.PERIOD3}));
    
    desc.declarePath(Values.TS, TS_LINE);
    desc.declarePath(Values.KS, KS_LINE);
    desc.declarePath(Values.CS, CS_LINE);
    desc.declarePath(Values.SSA, SSA_LINE);
    desc.declarePath(Values.SSB, SSB_LINE);
    
    desc.declareIndicator(Values.TS, TS_IND);
    desc.declareIndicator(Values.KS, KS_IND);
    desc.declareIndicator(Values.CS, CS_IND);
    desc.declareIndicator(Values.SSA, SSA_IND);
    desc.declareIndicator(Values.SSB, SSB_IND);
    
    desc.setRangeKeys(Values.TS, Values.KS, Values.CS, Values.SSA, Values.SSB);
    
    // Signals
    desc.declareSignal(Signals.TENKAN_CROSS_ABOVE_KIJUN, get("TENKAN_CROSS_ABOVE_KIJUN"));
    desc.declareSignal(Signals.TENKAN_CROSS_BELOW_KIJUN, get("TENKAN_CROSS_BELOW_KIJUN"));

    setRuntimeDescriptor(desc);
  }

  @Override
  public boolean isRepaintAllOnUpdate() { return true; }

  @Override
  public int getMinBars()
  {
    int forwardShift = getSettings().getInteger(Inputs.PERIOD2);
    return Util.maxInt(getSettings().getInteger(Inputs.PERIOD), getSettings().getInteger(Inputs.PERIOD2), getSettings().getInteger(Inputs.PERIOD3)) + forwardShift;
  }
  
  @Override
  protected void calculate(int index, DataContext ctx)
  {
    int period = getSettings().getInteger(Inputs.PERIOD);
    int period2 = getSettings().getInteger(Inputs.PERIOD2);
    int period3 = getSettings().getInteger(Inputs.PERIOD3);
    DataSeries series = ctx.getDataSeries();
    
    // Tenkan-Sen
    if (index >= period) {
      double highest = series.highest(index, period, Enums.BarInput.HIGH);
      double lowest = series.lowest(index, period, Enums.BarInput.LOW);
      series.setDouble(index, Values.TS, (highest + lowest)/2);
    }
    // Kijun-Sen
    if (index >= period2) {
      double highest = series.highest(index, period2, Enums.BarInput.HIGH);
      double lowest = series.lowest(index, period2, Enums.BarInput.LOW);
      series.setDouble(index, Values.KS, (highest + lowest)/2);
    }
    // Chikou Span
    if (index >= period2) {
      series.setDouble(index-period2, Values.CS, (double)series.getClose(index)); // shift backward by period2
    }

    // Senkou Span B
    if (index >= period3) {
      double highest = series.highest(index, period3, Enums.BarInput.HIGH);
      double lowest = series.lowest(index, period3, Enums.BarInput.LOW);
      series.setDouble(index+period2, Values.SSB, (highest + lowest)/2); // Shift Forward by period2
    }
    
    if (index <= period2) return;

    // Senkou Span A
    Double TS = series.getDouble(index, Values.TS);
    Double KS = series.getDouble(index, Values.KS);
    if (TS == null || KS == null) return;
    
    series.setDouble(index+period2, Values.SSA, (TS + KS)/2); // Shift forward by period2
    
    if (!series.isBarComplete(index)) return;
    
    // Check to see if a cross occurred and raise signal.
    Coordinate c = new Coordinate(series.getStartTime(index), KS);
    if (crossedAbove(series, index, Values.TS, Values.KS) && !series.getBoolean(index, Signals.TENKAN_CROSS_ABOVE_KIJUN, false)) {
      series.setBoolean(index, Signals.TENKAN_CROSS_ABOVE_KIJUN, true);
      MarkerInfo marker = getSettings().getMarker(Inputs.UP_MARKER);
      String msg = get("TENKAN_CROSS_ABOVE", format(TS), format(KS), format(series.getClose(index)));
      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
      ctx.signal(index, Signals.TENKAN_CROSS_ABOVE_KIJUN, msg, round(TS));
    }
    else if (crossedBelow(series, index, Values.TS, Values.KS) && !series.getBoolean(index, Signals.TENKAN_CROSS_BELOW_KIJUN, false)) {
      series.setBoolean(index, Signals.TENKAN_CROSS_BELOW_KIJUN, true);
      MarkerInfo marker = getSettings().getMarker(Inputs.DOWN_MARKER);
      String msg = get("TENKAN_CROSS_BELOW", format(TS), format(KS), format(series.getClose(index)));
      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
      ctx.signal(index, Signals.TENKAN_CROSS_BELOW_KIJUN, msg, TS);
    }

    series.setComplete(index, series.isBarComplete(index));
  }
}
