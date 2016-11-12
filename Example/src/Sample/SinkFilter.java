package Sample;

/******************************************************************************************************************
* File:SinkFilter.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Sample Pipe and Filter code (ajl).
*
* Description:
*
* This class serves as an example for using the SinkFilterTemplate for creating a sink filter. This particular
* filter reads some input from the filter's input port and does the following:
*
*	1) It parses the input stream and "decommutates" the measurement ID
*	2) It parses the input steam for measurments and "decommutates" measurements, storing the bits in a long word.
*
* This filter illustrates how to convert the byte stream data from the upstream filterinto useable data found in
* the stream: namely time (long type) and measurements (double type).
*
*
* Parameters: 	None
*
* Internal Methods: None
*
******************************************************************************************************************/
/******************************************************************************************************************
* File:SinkFilter.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Sample Pipe and Filter code (ajl).
*
* Description:
*
* This class serves as an example for using the SinkFilterTemplate for creating a sink filter. This particular
* filter reads some input from the filter's input port and does the following:
*
*	1) It parses the input stream and "decommutates" the measurement ID
*	2) It parses the input steam for measurments and "decommutates" measurements, storing the bits in a long word.
*
* This filter illustrates how to convert the byte stream data from the upstream filterinto useable data found in
* the stream: namely time (long type) and measurements (double type).
*
*
* Parameters: 	None
*
* Internal Methods: None
*
******************************************************************************************************************/
import java.util.*;						// This class is used to interpret time words
import java.text.SimpleDateFormat;		// This class is used to format and write time in a string format.

public class SinkFilter extends FilterFramework
{
	public void run()
    {
		/************************************************************************************
		*	TimeStamp is used to compute time using java.util's Calendar class.
		* 	TimeStampFormat is used to format the time value so that it can be easily printed
		*	to the terminal.
		*************************************************************************************/

		Calendar TimeStamp = Calendar.getInstance();
		SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy MM dd::hh:mm:ss:SSS");

		int MeasurementLength = 8;		// This is the length of all measurements (including time) in bytes
		int IdLength = 4;				// This is the length of IDs in the byte stream

		byte databyte = 0;				// This is the data byte read from the stream
		int bytesread = 0;				// This is the number of bytes read from the stream

		long measurement;				// This is the word used to store all measurements - conversions are illustrated.
		int id;							// This is the measurement id
		int i;							// This is a loop counter
		double alt_list[] = new double[100];
		double wild_list[]=new double[100];
		double temp_list[]=new double[100];
		double prs_list[]=new double[100];
		String time_stamp[]= new String[100];
		int t_s=0;
		int j=0;
		int prs_c=0;
		int wil_c=0;
		int tem_c = 0;
		/*************************************************************
		*	First we announce to the world that we are alive...
		**************************************************************/

		System.out.print( "\n" + this.getName() + "::Sink Reading ");

		while (true)
		{
			try
			{
				/***************************************************************************
				// We know that the first data coming to this filter is going to be an ID and
				// that it is IdLength long. So we first decommutate the ID bytes.
				****************************************************************************/

				id = 0;

				for (i=0; i<IdLength; i++ )
				{
					databyte = ReadFilterInputPort();	// This is where we read the byte from the stream...

					id = id | (databyte & 0xFF);		// We append the byte on to ID...

					if (i != IdLength-1)				// If this is not the last byte, then slide the
					{									// previously appended byte to the left by one byte
						id = id << 8;					// to make room for the next byte we append to the ID

					} // if

					bytesread++;						// Increment the byte count

				} // for

			

				measurement = 0;

				for (i=0; i<MeasurementLength; i++ )
				{
					databyte = ReadFilterInputPort();
					measurement = measurement | (databyte & 0xFF);	// We append the byte on to measurement...

					if (i != MeasurementLength-1)					// If this is not the last byte, then slide the
					{												// previously appended byte to the left by one byte
						measurement = measurement << 8;				// to make room for the next byte we append to the
																	// measurement
					} // if

					bytesread++;									// Increment the byte count

				} // for
				
				/**
				 * Here we are looking for the time with measurement id=0 in the source file
				 */

				if ( id == 0 )
				{
					TimeStamp.setTimeInMillis(measurement);
					time_stamp[t_s]= TimeStampFormat.format(TimeStamp.getTime());
				} // if
				/**
				 * Here we are looking for the altitude with measurement id=2 in the source file
				 * and converting it into meters
				 */
				
				if(id==2)//Gets altitude from the frame data set
				{
					alt_list[j]=Double.longBitsToDouble(measurement)*0.3048;
					j++;
					
				}
				/**
				 * Here we are looking for the pressure with measurement id=3 in the source file
				 */
				if(id==3)//Gets pressure from the frame data set
				{
				prs_list[prs_c]=Double.longBitsToDouble(measurement);
				prs_c++;
				}
				/**
				 * Here we are looking for the temperature with measurement id=2 in the source file
				 * And converting it into celsius scale
				 */
				if(id==4)//Gets temp from data set
				{
				temp_list[tem_c]=(Double.longBitsToDouble(measurement)-32)*0.5555555;
				tem_c++;
				}
				
				
				
				
			} // try

			/*******************************************************************************
			*	The EndOfStreamExeception below is thrown when you reach end of the input
			*	stream (duh). At this point, the filter ports are closed and a message is
			*	written letting the user know what is going on.
			********************************************************************************/

			catch (EndOfStreamException e)
			{
				ClosePorts();
				System.out.print( "\n" + this.getName() + "::Sink Exiting; bytes read: " + bytesread);
				
				/**
				 * We check the wild jumps fro altitude with 100m difference. 
				 */
	
				int c=0;//Variable to increment the array list index
				if(c==0)
				{
					System.out.println("Time stamp"+time_stamp[c]+" Altitude Value :"+alt_list[c]+" Pressure value"+prs_list[c]+" Temperature value:"+temp_list[c]);
					
				}
				for(c=1;c<99;c++)
				{
					if(alt_list[c]-alt_list[c-1]>100)
					{
						wild_list[wil_c]=alt_list[c];
						wil_c++;
						alt_list[c]=(alt_list[c-1]+alt_list[c+1])/2;
						System.out.println("Time stamp"+time_stamp[c]+" altitude with wild jump"+alt_list[c]+" Pressure value"+prs_list[c]+" Temperature value:"+temp_list[c]);
					}
					else{
						System.out.println("Time stamp"+time_stamp[c]+" altitude without wild jump"+alt_list[c]+" Pressure value"+prs_list[c]+" Temperature value:"+temp_list[c]);
					}
					
				}
				if(c==99)
				{
					if(alt_list[c]-alt_list[c-1]>100)
					{
						wild_list[wil_c]=alt_list[c];
						alt_list[c]=alt_list[c-1];
						System.out.println("Time stamp"+time_stamp[c]+" Last value of altitude is wild jump"+alt_list[c]+" Pressure value"+prs_list[c]+" Temperature value:"+temp_list[c]);
					}
					else{
						System.out.println("Time stamp"+time_stamp[c]+" Last value of altitude without wild jump"+alt_list[c]+" Pressure value"+prs_list[c]+" Temperature value:"+temp_list[c]);
					}
				}
				System.out.println("--------------------------------------------");
				System.out.println("Rejected altitude values are below:");
				for(int count=0;count<wil_c;count++)
				{
					System.out.println(wild_list[count]);
				}
				break;

			} // catch
		
			
		} // while
	
	


   } // run

} // SingFilter