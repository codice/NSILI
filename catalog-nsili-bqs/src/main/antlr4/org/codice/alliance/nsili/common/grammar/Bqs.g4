grammar Bqs;
/*
STANAG 4559 BQS GRAMMAR

query ::= term { "or" term }
term ::= factor { "and" factor }
factor ::= ["not"] primary
primary ::= ( simple_attribute_name comp_op constant_expression )
( geo_attribute_name geo_op geo_element)
| ( geo_attribute_name rel_geo_op number dist_units “of” geo_element) | ( text_attribute_name "like" quoted_string )
| ( attribute_name “exists”) | ( "(" query ")" )
attribute ::= a member of the set of queryable attribute names (defined in the appropriate GIAS profile)
attribute_name ::= attribute | {entity “:”}entity “.” attribute
simple_attribute_name ::= member of subset of attribute_name for which boolean operators (comp_op) are allowed
geo_attribute_name ::= member of subset of attribute_name for which geospatial operators are allowed
text_attribute_name ::= member of subset of attribute_name for which string operators are allowed (“free text search”)
comp_op ::= "=" | "<" | ">" | "<>" | "<=" | ">="
constant_expression ::= number | quoted_string | date
date ::= “’” year “/” month “/” day [“<blank>” hour “:” minute “:” second]”’” year ::= digit digit digit digit
month ::= digit digit
day ::= digit digit
hour ::= digit digit minute ::= digit digit
  F-2
Amendment 2
second ::= digit digit [“.” digit {digit}]
geo_op ::= “intersect” | “outside”| “inside”
rel_geo_op ::= “within” | “beyond”
dist_units ::= “feet” | “meters” | “statute miles” | “nautical miles” | “kilometers”
geo_element ::= point | polygon | rectangle | circle | ellipse | line | polygon_set
sign ::= “+” | “-”
number ::= [sign] digit_seq [ "." [ digit_seq ] ]
digit_seq ::= digit { digit }
digit ::= “0” | “1” | “2” | “3” | “4” | “5” | “6” | “7” | “8” | ” 9”
quoted_string ::= "'" { character } "'" // Single quotes
character ::= “a”|”b”| .... // All printable ASCII characters To use a "'" (single quote) use "''" (two single quotes)
Del = “,” // Delimiter
latitude ::= number
longitude ::= number
hemi ::= “N” | “S” | “E” | “W”
DMS ::= [digit] digit digit “:” digit digit “:” digit digit “.” digit hemi latitude_DMS ::= DMS
longitude_DMS ::= DMS
latlon ::= latitude Del longitude | latitude_DMS Del longitude_DMS
coordinate ::= latlon
point :: = “POINT” “(“ coordinate “)”
polygon ::= “POLYGON” “(“ coordinate Del coordinate Del coordinate {Del
coordinate}“)”
rectangle ::= “RECTANGLE” “(“ upper_left Del lower_right “)”
upper_left ::= coordinate
lower_right ::= coordinate
circle ::= “CIRCLE” “(“ coordinate Del radius “)”
radius ::= number dist_units
ellipse ::= “ELLIPSE” “(“ coordinate Del major_axis_len Del minor_axis_len Del north_angle “)”
major_axis_len ::= number dist_units minor_axis_len ::= number dist_units north_angle ::= number
number is given in decimal degrees north is 0 and direction is clockwise range is 0 to 360
line ::= “LINE” “(“ coordinate Del coordinate { Del coordinate} “)” polygon_set ::= “POLYGON_SET” “(“ polygon { Del polygon} “)”
*/

options {
   k=10;
}
start_term : query;
query : term (BLANK OR BLANK term)*;
term : factor (BLANK AND BLANK factor)* ;
factor : NOT BLANK primary | primary;
primary :
    attribute_name BLANK comp_op BLANK constant_expression
    | attribute_name BLANK geo_op BLANK geo_element
    | attribute_name BLANK rel_geo_op BLANK number BLANK dist_units BLANK OF BLANK geo_element
    | attribute_name BLANK LIKE BLANK quoted_string
    | attribute_name BLANK EXISTS
    | LPAREN query RPAREN;
attribute_name : CHARACTER | (CHARACTER UNDERSCORE CHARACTER COLON)* (CHARACTER UNDERSCORE)+ CHARACTER DOT CHARACTER ;
comp_op : EQUAL | LT | GT | NOTEQ | LTE | GTE;
constant_expression : number | date | quoted_string;
date : DATE;
geo_op : INTERSECT | OUTSIDE | INSIDE;
rel_geo_op : WITHIN | BEYOND;
dist_units : FEET | METERS | STATUTE_MI | NAUTICAL_MI | KILOMETER | METERS_UPPER | FEET_UPPER;
geo_element : point | polygon | rectangle | circle | ellipse | line | polygon_set;
sign : PLUS | MINUS;
number : (sign)* digit_seq ( DOT digit_seq )*;
digit_seq : ( DIGIT )+;
quoted_string : QUOTED_STRING;
latitude : number;
longitude : number;
dms : ( DIGIT )* DIGIT DIGIT COLON DIGIT DIGIT COLON DIGIT DIGIT DOT DIGIT hemi;
latitude_DMS : dms;
longitude_DMS : dms;
latlon : latitude Del longitude | latitude_DMS Del longitude_DMS;
coordinate : latlon;
point : POINT LPAREN coordinate RPAREN;
polygon : POLYGON LPAREN coordinate Del coordinate Del coordinate (Del coordinate)* RPAREN;
rectangle : RECTANGLE LPAREN upper_left Del lower_right RPAREN;
upper_left : coordinate;
lower_right : coordinate;
circle : CIRCLE LPAREN coordinate Del radius RPAREN;
radius : number BLANK dist_units;
ellipse : ELLIPSE LPAREN coordinate Del major_axis_len Del minor_axis_len Del north_angle RPAREN;
major_axis_len : number BLANK dist_units;
minor_axis_len : number BLANK dist_units;
north_angle : number;
line : LINE LPAREN coordinate Del coordinate ( Del coordinate )* RPAREN;
polygon_set : POLYGON_SET LPAREN polygon ( Del polygon )* RPAREN;
search_character : (SEARCH_CHARACTER)*;
OR : 'or' | 'OR';
AND : 'and' | 'AND';
NOT : 'not' | 'NOT';
OF : 'of' | 'OF';
LIKE : 'like' | 'LIKE';
COLON : ':';
DOT : '.';
EXISTS : 'exists' | 'EXISTS';
DIGIT : '0'..'9';
hemi : 'N' | 'S' | 'E' | 'W';
LPAREN : '(';
RPAREN : ')';
EQUAL : '=';
LT : '<';
GT : '>';
NOTEQ : '<>';
LTE : '<=';
GTE : '>=';
SINGLEQT : '\'';
SLASH : '/';
Del : ',';
POINT : 'POINT';
POLYGON : 'POLYGON';
RECTANGLE : 'RECTANGLE';
CIRCLE : 'CIRCLE';
ELLIPSE : 'ELLIPSE';
LINE : 'LINE';
POLYGON_SET : 'POLYGON_SET';
INTERSECT : 'intersect';
OUTSIDE : 'outside';
INSIDE : 'inside';
WITHIN : 'within';
BEYOND : 'beyond';
FEET : 'feet';
METERS : 'meters';
STATUTE_MI : 'statute miles';
NAUTICAL_MI : 'nautical miles';
KILOMETER : 'kilometers';
METERS_UPPER : 'METERS';
FEET_UPPER : 'FEET';
PLUS : '+';
MINUS : '-';
BLANK : ' ';
CHARACTER : ('a'..'z' | 'A'..'Z')+;
UNDERSCORE : '_';
PERCENT : '%';
DATE : SINGLEQT DIGIT DIGIT DIGIT DIGIT SLASH DIGIT DIGIT SLASH DIGIT DIGIT (BLANK DIGIT DIGIT COLON DIGIT DIGIT COLON DIGIT DIGIT (DOT (DIGIT)+ )*)* SINGLEQT;
SEARCH_CHARACTER : ' '..'~';
QUOTED_STRING : SINGLEQT ('\\$'|.)*? SINGLEQT;