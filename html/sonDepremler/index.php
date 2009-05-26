<?php
header('Content-Type: text/html; charset=windows-1254');

function getLatestQuakes(){

  //read in quake info  
  $fh = fopen("http://www.koeri.boun.edu.tr/scripts/sondepremler.asp", "r");
  //$fh = fopen("tmp.dat", "r");

  while(!feof($fh)){
    $data = $data . fgets($fh, 1024);
  }

  fclose($fh);

  //parse input
  $dataBegin = stripos($data , "------------    -----------");
  $dataBegin = $dataBegin + 27;

  $dataEnd = stripos($data , "</pre>" , $dataBegin );

  $data = substr($data , $dataBegin , $dataEnd - $dataBegin );
  $data = trim($data);
  
  return $data;
  }

function readLatestQuakes($cache_file){

  //read in quake info  
  $fh = fopen( $cache_file , "r");

  while(!feof($fh)){
    $data = $data . fgets($fh, 1024);
  }

  fclose($fh);
  return $data;
}

function writeLatestQuakes($data , $cache_file ){

  $fh = fopen( $cache_file , 'w');
  fwrite($fh,$data);
  fclose($fh);
}

function prepareQuakeData($data){

  $quakeData = Array();

  $data = preg_replace("/ {2,}/", " ", $data);
  $lines = explode( "\n" , $data );

  for( $i = 0; $i< count($lines) ; $i+=1 ){
    //print $lines[$i] . "<br>"

    $items = explode(" " , $lines[$i] , 9 );
    for( $j = 0; $j< count($items) ; $j+=1 ){
      $quakeData[$i][$j] = preg_replace("/\s*/", "", $items[$j]);
    }          

  }
  return $quakeData;
}

function initializeQuakes( ){
  $cache_file = "cache.dat";
  $cache_time = 15*60; // 15 minutes in seconds
  $data = "";

  if(@file_exists( $cache_file) && time() - $cache_time < @filemtime($cache_file)) {
    $data = readLatestQuakes($cache_file);
  }else{
    //if cache expired recreate
    $data = getLatestQuakes();
    writeLatestQuakes($data,$cache_file);
  }

  $quakeData = prepareQuakeData($data);
  /* print count($quakeData) . "<br>\n"; */

  /* for($i = 0; $i < count($quakeData); $i+=1 ){ */
  /*   print $i . " " . "<br>"; */

  /*   for($j = 0; $j < 9 ; $j+=1 ){ */
  /*     print $quakeData[$i][$j] . "<br>"; */
  /*   } */
    
  /* } */


  return $quakeData;
}


//print initializeQuakes();

?>

<!DOCTYPE html "-//W3C//DTD XHTML 1.0 Strict//EN" 
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>

  <style type="test/css">
  body { font-family: arial; font-size: 10px; }
  </style>

    <meta http-equiv="Content-Type" content="text/html; charset=windows-1254">
    <meta http-equiv="Content-Language" content="tr">
    <META NAME="description" CONTENT="Turkiyede yasanan son depremler.">
    <META NAME="keywords" CONTENT="deprem, turkiye, google maps, harita">

    <title>Son Depremler</title>


  <script src="http://maps.google.com/maps?file=api&amp;v=2&amp;sensor=false
    &amp;key=ABQIAAAAxZe85aHltkG56-i32xsNMhSzjREPkRTrqeS0z6fvrdfEdQ7XUhSTl505eiSzZuWaJqP8h5v0pgYesg"
    type="text/javascript">
  </script>
  <script type='text/javascript' src='HtmlControl.js'></script>


    <script type="text/javascript">

    function initialize() {
      if (GBrowserIsCompatible()) {


	function createMarker(point,icon,html,title) {
	  var opt = {title:title, icon:icon};

	  var marker = new GMarker(point,opt);
	  GEvent.addListener(marker, "click", function() {
	      marker.openInfoWindowHtml(html);
	    });
	  return marker;
	}

	function tinyImage(opt_color, opt_preload){
	  var color = opt_color||"red";
	  var src_ = "http://labs.google.com/ridefinder/images/mm_20_"+color+".png";
	  if(opt_preload){
	    var preImage = new Image();
	    preImage.src = src_;
	  }
	  return  src_;
	} 

	function tinyIcon(opt_color){
	  var tiny = new GIcon();
	  tiny.image = tinyImage(opt_color);
	  tiny.shadow = "http://labs.google.com/ridefinder/images/mm_20_shadow.png";
	  tiny.iconSize = new GSize(12, 20);
	  tiny.shadowSize = new GSize(22, 20);
	  tiny.iconAnchor = new GPoint(6, 20);
	  tiny.infoWindowAnchor = new GPoint(5, 1);
	  tiny.imageMap = [4,0,0,4,0,7,3,11,4,19,7,19,8,11,11,7,11,4,7,0];
	  tiny.transparent = "http://maps.google.com/mapfiles/transparent.png"; 
	  return tiny;
	}

	function addGroup(map,grp){

	  for (var i=0;i< grp.length;i++){
	    map.addOverlay(grp[i]);
	  }

	}

	function removeGroup(map,grp){

	  for (var i=0;i< grp.length;i++){
	    map.removeOverlay(grp[i]);
	  }

	}

        var map = new GMap2(document.getElementById("map_canvas"));
	map.setMapType(G_SATELLITE_MAP);
        map.setCenter(new GLatLng(39.3113, 32.8038), 7);
        map.setUIToDefault();


	var grp1 = new Array();
	var grp2 = new Array();
	var grp3 = new Array();
	var grp4 = new Array();
	var grp5 = new Array();


<?php
	$quakeData = initializeQuakes();
	
	for($i = 0; $i < count($quakeData) ; $i+=1 ){
	  $lat = $quakeData[$i][2];
	  $long = $quakeData[$i][3];

	  $location = $quakeData[$i][8];
	  $size = $quakeData[$i][5]."/".$quakeData[$i][6]."/".$quakeData[$i][7];
	  $depth = $quakeData[$i][4];
	  $date = $quakeData[$i][0] . "/". $quakeData[$i][1];

	  print "var point = new GLatLng($lat,$long);\n";

	  if ( $i == 0 )
	    print "var icon = tinyIcon(\"red\");";
	  else
	    print "var icon = tinyIcon(\"green\");";

	  print "var html='";
	  print "<div style=\"font-family: arial; font-size: 12px;\">";
	  print "<br><b>$location</b><br>";
	  print "<br>Buyukluk: $size MD/ML/MS";
	  print "<br>Derinlik: $depth km";
	  print "<br><br> $date";
	  print "</div>';";
	  print "var latestQuakeInfo=html;";

	  print "var marker = createMarker(point,icon,html,\"$location\");\n";
	  
	  if ( $i >= 0 && $i <= 9 )
	    print "grp1[$i] = marker;";

	  if ( $i >= 10 && $i <= 49 ){	    
	    $index = $i-10;
	    print "grp2[$index] = marker;";
	  }

	  if ( $i >= 50 && $i <= 99 ){
	    $index = $i-50;
	    print "grp3[$index] = marker;";
	  }

	  if ( $i >= 100 && $i <= 149 ){
	    $index = $i-100;
	    print "grp4[$index] = marker;";
	  }

	  if ( $i >= 150 && $i <= 199 ){
	    $index = $i-150;
	    print "grp5[$index] = marker;";
	  }

	  //print "map.addOverlay(marker);";
	}

	print "addGroup(map,grp1);";
	print "grp1[0].openInfoWindow(latestQuakeInfo);";

?>

	var infoMessage = '<p id="infoMessage"' 
	+ ' style="font-weight:bold;color:white;font-family: arial;'
	+' font-size: 18px;">Gozuken Deprem Sayisi: 10</p>'

	var control = new HtmlControl(infoMessage);
	var position = new GControlPosition(G_ANCHOR_TOP_LEFT, new GSize(80,0));
        map.addControl(control , position );


	infoMessage = '<a href=\"#\"><img id=\"lessView\" src=\"icons/lt.png\" border=0 ></a>'
	control = new HtmlControl(infoMessage);
	position = new GControlPosition(G_ANCHOR_TOP_LEFT, new GSize(80,50));
        map.addControl(control , position );

	infoMessage = '<a href=\"#\"><img id=\"moreView\" src=\"icons/rt.png\" border=0 ></a>'
	control = new HtmlControl(infoMessage);
	position = new GControlPosition(G_ANCHOR_TOP_LEFT, new GSize(130,50));
        map.addControl(control , position );

	//add view logic
	var visibleBucket = 1;

	GEvent.addDomListener
	(document.getElementById('moreView'), 'click', function() {
	  var message = "Gozuken Deprem Sayisi: "; 

	  if (visibleBucket == 1 ){
	    message = message + "50";
	    visibleBucket = 2;
	    addGroup(map,grp2);
	  } else if (visibleBucket == 2 ){
	    message = message + "100";
	    visibleBucket = 3;
	    addGroup(map,grp3);
	  }else if (visibleBucket == 3 ){
	    message = message + "150";
	    visibleBucket = 4;
	    addGroup(map,grp4);
	  }else if (visibleBucket >= 4 ){
	    message = message + "200";
	    visibleBucket = 5;
	    addGroup(map,grp5);
	  }

	  document.getElementById('infoMessage')
	    .innerHTML= message;


	});

	GEvent.addDomListener
	(document.getElementById('lessView'), 'click', function() {
	  var message = "Gozuken Deprem Sayisi: "; 

	  if (visibleBucket == 5 ){
	    message = message + "150";
	    visibleBucket = 4;
	    removeGroup(map,grp5);
	  } else if (visibleBucket == 4 ){
	    message = message + "100";
	    visibleBucket = 3;
	    removeGroup(map,grp4);
	  }else if (visibleBucket == 3 ){
	    message = message + "50";
	    visibleBucket = 2;
	    removeGroup(map,grp3);
	  }else if (visibleBucket <= 2 ){
	    message = message + "10";
	    visibleBucket = 1;
	    removeGroup(map,grp2);
	  }

	  document.getElementById('infoMessage')
	    .innerHTML= message;


	});

	//init end
      }
    }

    </script>


  </head>
  <body onload="initialize()" onunload="GUnload()">
    <div id="map_canvas" style="width: 100%; height: 100%"></div>
  </body>
</html>