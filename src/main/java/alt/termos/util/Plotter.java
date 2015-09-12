package alt.termos.util;

import java.util.Iterator;

public class Plotter {

	public static StringBuilder chart(String name, Iterator<Double> iterator) {
		
		  StringBuilder htmlContents = new StringBuilder();
		  htmlContents.append("<html>");
		  htmlContents.append("<meta http-equiv=\"refresh\" content=\"100\">");
		  htmlContents.append("<head>"); 
		  htmlContents.append("<script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>");
		  htmlContents.append("<script type=\"text/javascript\">"); 
		  htmlContents.append("google.load(\"visualization\", \"1\", {packages:[\"corechart\"]});");
		  htmlContents.append("google.setOnLoadCallback(drawChart);");
		  htmlContents.append("function drawChart() {" );
		  htmlContents.append("var data = google.visualization.arrayToDataTable([" );
		  htmlContents.append("['i', 'Value']," );
		
		  int i = 0;
		  while(iterator.hasNext()) {
			  Double value = iterator.next();

			  htmlContents.append('[');
			  htmlContents.append(i++);
			  htmlContents.append(", ");
			  DoubleShortter.append(htmlContents, DoubleShortter.format(value));  
			  htmlContents.append("],");

		  }
		  
		  htmlContents.setLength(htmlContents.length()-1);
	  
		  htmlContents.append("]);");
		  htmlContents.append("var options = {");
		  htmlContents.append("title: '");
		  htmlContents.append(name);
		  htmlContents.append("'}; var chart = new google.visualization.LineChart(document.getElementById('chart_div')); ");
		  htmlContents.append("chart.draw(data, options); } </script> </head> <body> ");
		  htmlContents.append("<div id=\"chart_div\" style=\"width: 1500px; height: 700px;\"></div>");
		  htmlContents.append("</body> </html>");
		  
		  return htmlContents;
		  
	}
}
