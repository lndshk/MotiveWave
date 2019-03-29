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

//A Java class that can return multiple values of different types
//encapsulate the items into a new object class
//and returning an object of class. 

//A class that is used to store and return 
//members of different types 

class SMIinput { 
	int hlPeriod; // To store the High/Low Period
	int maPeriod; // To store the period of the moving average
	int smoothPeriod; // To store the smoothingPeriod

	SMIinput(int hl, int ma, int sp) 
	{ 
		hlPeriod = hl; 
		maPeriod = ma; 
		smoothPeriod = sp; 
	} 
} 

class SMIseries {
	//Enums.MAMethod method; // = getSettings().getMAMethod(Inputs.METHOD);
	DataSeries Dsig; // These values are used for calculating averages and smoothed averages
    DataSeries HLsig;
    DataSeries D_MAsig;
    DataSeries HL_MAsig;
        
	SMIseries(DataSeries D, DataSeries HL, DataSeries D_MA, DataSeries HL_MA)
	{
	Dsig = D;
	HLsig = HL;
	D_MAsig = D_MA;
	HL_MAsig = HL_MA;
	
	}
}

class SMIoutput {
	//Enums.MAMethod method; // = getSettings().getMAMethod(Inputs.METHOD);
	DataSeries SMIsig; // These values are used for calculating averages and smoothed averages
    DataSeries SIGsig;
    
	SMIoutput(DataSeries SMI, DataSeries SIG)
	{
	SMIsig = SMI;
	SIGsig = SIG;
	}
}

public class vrSMI {

	static SMIoutput getvrSMI(SMIinput a, SMIseries s, int index, DataContext ctx)
	{	
		    if (index < a.hl) return; //return if the index is less than the high period

		    DataSeries series = ctx.getDataSeries();
		    double HH = series.highest(index, a.hl, Enums.BarInput.HIGH);
		    double LL = series.lowest(index, a.hl, Enums.BarInput.LOW);
		    double M = (HH + LL)/2.0;
		    double D = series.getClose(index) - M;
	
		    series.setDouble(index, s.D, D);
		    series.setDouble(index, s.HL, HH - LL);
	
		    if (index < a.hl + a.ma) return;
	
	
	
	
	
	
	
	
	
}








class Test { 
	static MultiDivAdd getMultDivAdd(int a, int b) 
	{ 
		// Returning multiple values of different 
		// types by returning an object 
		return new MultiDivAdd(a * b, (double)a / b, (a + b)); 
	} 

	/*
	// Driver code 
	public static void main(String[] args) 
	{ 
		MultiDivAdd ans = getMultDivAdd(10, 20); 
		System.out.println("Multiplication = " + ans.mul); 
		System.out.println("Division = " + ans.div); 
		System.out.println("Addition = " + ans.add); 
	} 
	*/
} 


