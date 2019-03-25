package com.motivewave.platform.study.ma;

import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums.MAMethod;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Calculates and displays the Kauffman Adaptive Moving Average and plots as a line on the price graph. */
@StudyHeader(
 namespace="com.motivewave", 
 id="KAMA", 
 rb="com.motivewave.platform.study.nls.strings",
 name="TITLE_KAMA",
 label="LBL_KAMA",
 desc="DESC_KAMA",
 menu="MENU_MOVING_AVERAGE",
 overlay=true,
 signals=true,
 studyOverlay=true,
 helpLink="http://www.motivewave.com/studies/kaufman_adaptive_moving_average.htm")
public class KAMA extends MABase
{
  @Override
  public void initialize(Defaults defaults)
  {
    METHOD = MAMethod.KAMA;
    MA_LABEL = get("LBL_KAMA");
    super.initialize(defaults);
  }
}
