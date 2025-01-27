grammar Rain;

project : '[' PROJECT ID ']' client? server* EOF ;

positionX : POSITION_X '=' NEGATIVE? NUMBER_PX ';' ;
positionY : POSITION_Y '=' NEGATIVE? NUMBER_PX ';' ;
positionSorting : POSITION_SORTING '=' NUMBER ';' ;
position : POSITION BLOCK_START positionX positionY positionSorting BLOCK_END ;

codeModules : CODE_MODULES BLOCK_START CODE BLOCK_END ;

blueprintComponents : SECTION_BLUEPRINT_COMPONENTS blueprintComponent+ ;
blueprintElement : shape | blueprintComponent ;
blueprintComponent : BLUEPRINT_COMPONENT ID BLOCK_START position? blueprintElement* BLOCK_END;

element : (shape | component) ;
component : COMPONENT ID BLOCK_START position element* codeModules? BLOCK_END ;
positionMutation : POSITION BLOCK_START positionX? positionY? BLOCK_END ;
componentMutation : COMPONENT_MUTATION ID BLOCK_START positionMutation BLOCK_END ;
shapeMutation : SHAPE_MUTATION ID BLOCK_START positionMutation? (shapeRectangleProperties | shapeCircleProperties | shapeTextProperties) BLOCK_END ;
componentReference : COMPONENT ID REFERENCES ID BLOCK_START position (componentMutation | shapeMutation)* BLOCK_END ;

propertyBackgroundColor : PROPERTY_BACKGROUND_COLOR_KEY '=' COLOR ';' ;
propertyBorderColor : PROPERTY_BORDER_COLOR_KEY '=' COLOR ';' ;
propertyShadowColor : PROPERTY_SHADOW_COLOR_KEY '=' COLOR ';' ;
propertyBorderRadiusValue : NUMBER_PX ',' NUMBER_PX ',' NUMBER_PX ',' NUMBER_PX ;
propertyBorderRadius : PROPERTY_BORDER_RADIUS_KEY '=' propertyBorderRadiusValue ';' ;
propertyHeight : PROPERTY_HEIGHT_KEY '=' NUMBER_PX ';' ;
propertyWidth : PROPERTY_WIDTH_KEY '=' NUMBER_PX ';' ;
propertyBorderWidth : PROPERTY_BORDER_WIDTH_KEY '=' NUMBER_PX ';' ;
propertyShadowX : PROPERTY_SHADOW_X_KEY '=' NEGATIVE? NUMBER_PX ';' ;
propertyShadowY : PROPERTY_SHADOW_Y_KEY '=' NEGATIVE? NUMBER_PX ';' ;
propertyShadowBlur : PROPERTY_SHADOW_BLUR_KEY '=' NUMBER_PX ';' ;
boolean : BOOLEAN_TRUE | BOOLEAN_FALSE ;
propertyBackgroundColorEnabled : PROPERTY_BACKGROUND_COLOR_ENABLED_KEY '=' boolean ';' ;
propertyBorderEnabled : PROPERTY_BORDER_ENABLED_KEY '=' boolean ';' ;
propertyShadowEnabled : PROPERTY_SHADOW_ENABLED_KEY '=' boolean ';' ;
propertyVisible : PROPERTY_VISIBLE_KEY '=' boolean ';' ;
propertyOpacity : PROPERTY_OPACITY_KEY '=' NUMBER_PERCENT ';' ;
propertyFontFamily : PROPERTY_FONT_FAMILY_KEY '=' PROPERTY_FONT_FAMILY_VALUE ';' ;
propertyFontStyle : PROPERTY_FONT_STYLE_KEY '=' PROPERTY_FONT_STYLE_VALUE ';' ;
propertyTextDecoration : PROPERTY_TEXT_DECORATION_KEY '=' PROPERTY_TEXT_DECORATION_VALUE ';' ;
propertyTextTransform : PROPERTY_TEXT_TRANSFORM_KEY '=' PROPERTY_TEXT_TRANSFORM_VALUE ';' ;
propertyTextAlign : PROPERTY_TEXT_ALIGN_KEY '=' PROPERTY_TEXT_ALIGN_VALUE ';' ;
propertyTextColor : PROPERTY_TEXT_COLOR_KEY '=' COLOR ';' ;
propertyFontSize : PROPERTY_FONT_SIZE_KEY '=' NUMBER_PX ';' ;
propertyText : PROPERTY_TEXT_KEY '=' PROPERTY_TEXT_VALUE ';' ;

shapeRectangleProperties : SHAPE_PROPERTIES BLOCK_START (
  propertyBackgroundColor
  | propertyBorderColor
  | propertyShadowColor
  | propertyBorderRadius
  | propertyHeight
  | propertyWidth
  | propertyBorderWidth
  | propertyShadowX
  | propertyShadowY
  | propertyShadowBlur
  | propertyBackgroundColorEnabled
  | propertyBorderEnabled
  | propertyShadowEnabled
  | propertyVisible
  | propertyOpacity
)+ BLOCK_END ;
shapeRectangle : SHAPE SHAPE_RECTANGLE ID BLOCK_START position shapeRectangleProperties BLOCK_END ;

shapeCircleProperties : SHAPE_PROPERTIES BLOCK_START (
  propertyBackgroundColor
  | propertyBorderColor
  | propertyShadowColor
  | propertyHeight
  | propertyWidth
  | propertyBorderWidth
  | propertyShadowX
  | propertyShadowY
  | propertyShadowBlur
  | propertyBackgroundColorEnabled
  | propertyBorderEnabled
  | propertyShadowEnabled
  | propertyVisible
  | propertyOpacity
)+ BLOCK_END ;
shapeCircle : SHAPE SHAPE_CIRCLE ID BLOCK_START position shapeCircleProperties BLOCK_END ;

shapeTextProperties : SHAPE_PROPERTIES BLOCK_START (
  propertyBackgroundColor
  | propertyBorderColor
  | propertyShadowColor
  | propertyHeight
  | propertyWidth
  | propertyBorderWidth
  | propertyShadowX
  | propertyShadowY
  | propertyShadowBlur
  | propertyBackgroundColorEnabled
  | propertyBorderEnabled
  | propertyShadowEnabled
  | propertyVisible
  | propertyOpacity
  | propertyFontFamily
  | propertyFontStyle
  | propertyTextDecoration
  | propertyTextTransform
  | propertyTextAlign
  | propertyTextColor
  | propertyFontSize
  | propertyText
)+ BLOCK_END ;
shapeText : SHAPE SHAPE_TEXT ID BLOCK_START position shapeTextProperties BLOCK_END ;

shape : shapeRectangle | shapeCircle | shapeText ;

componentElement: (component | componentReference) ;
view : '[' VIEW ROOT? ID propertyHeight propertyWidth ']' componentElement* ;

client : '[' CLIENT name=ID ']' blueprintComponents? view* ;

bootsSpecification : BOOTS_SPECIFICATION BLOCK_START CODE BLOCK_END ;
umbrellaSpecification : UMBRELLA_SPECIFICATION BLOCK_START CODE BLOCK_END ;

route : ROUTE (
                ROUTE_GET
              | ROUTE_POST
              | ROUTE_PUT
              | ROUTE_DELETE
              ) ID BLOCK_START bootsSpecification? umbrellaSpecification? codeModules BLOCK_END ;
server : '[' SERVER name=ID '@' (hostname=ID | ipAddress=SERVER_IP) (':' NUMBER)? ']' route+ ;

BLOCK_START : '{' ;
BLOCK_END : '}' ;

WHITESPACE : [ \r\t\n]+ -> skip ;
COMMENT_SINGLE_LINE : '//' ~[\r\n]* '\r'? '\n' -> skip ;

SERVER_IP : [0-9]+ '.' [0-9]+ '.' [0-9]+ '.' [0-9]+ ;
NUMBER : [0-9]+ ;

PROJECT : 'project' ;
CLIENT : 'client' ;
SECTION_BLUEPRINT_COMPONENTS : '[blueprint components]' ;
BLUEPRINT_COMPONENT : 'blueprint component' ;

PROPERTY_BACKGROUND_COLOR_KEY : 'background color' ;
fragment COLOR_CHARACTER : [0-9a-f] ;
COLOR : '#' COLOR_CHARACTER COLOR_CHARACTER COLOR_CHARACTER COLOR_CHARACTER COLOR_CHARACTER COLOR_CHARACTER ;
PROPERTY_BORDER_COLOR_KEY : 'border color' ;
PROPERTY_SHADOW_COLOR_KEY : 'shadow color' ;

PROPERTY_BORDER_RADIUS_KEY : 'border radius' ;
NEGATIVE : '-' ;
NUMBER_PX : [0-9]+ 'px' ;

PROPERTY_HEIGHT_KEY : 'height' ;
PROPERTY_WIDTH_KEY : 'width' ;
PROPERTY_BORDER_WIDTH_KEY : 'border width' ;
PROPERTY_SHADOW_X_KEY : 'shadow x' ;
PROPERTY_SHADOW_Y_KEY : 'shadow y' ;
PROPERTY_SHADOW_BLUR_KEY : 'shadow blur' ;

PROPERTY_BACKGROUND_COLOR_ENABLED_KEY : 'background color enabled' ;
BOOLEAN_TRUE : 'true' ;
BOOLEAN_FALSE : 'false' ;
PROPERTY_BORDER_ENABLED_KEY : 'border enabled' ;
PROPERTY_SHADOW_ENABLED_KEY : 'shadow enabled' ;
PROPERTY_VISIBLE_KEY : 'visible' ;

PROPERTY_OPACITY_KEY : 'opacity' ;
NUMBER_PERCENT : [0-9][0-9]?[0-9]? '%' ;

PROPERTY_FONT_FAMILY_KEY : 'font family' ;
fragment FONT_FAMILY_ARIAL : 'arial' ;
PROPERTY_FONT_FAMILY_VALUE : FONT_FAMILY_ARIAL ;

PROPERTY_FONT_STYLE_KEY : 'font style' ;
fragment FONT_STYLE_NORMAL : 'normal' ;
fragment FONT_STYLE_BOLD : 'bold' ;
fragment FONT_STYLE_ITALIC : 'italic' ;
PROPERTY_FONT_STYLE_VALUE : FONT_STYLE_NORMAL | FONT_STYLE_BOLD | FONT_STYLE_ITALIC ;

PROPERTY_TEXT_DECORATION_KEY : 'text decoration' ;
fragment NONE : 'none' ;
fragment TEXT_DECORATION_UNDERLINE : 'underline' ;
fragment TEXT_DECORATION_STRIKE_THROUGH : 'strike through' ;
PROPERTY_TEXT_DECORATION_VALUE : NONE | TEXT_DECORATION_UNDERLINE | TEXT_DECORATION_STRIKE_THROUGH ;

PROPERTY_TEXT_TRANSFORM_KEY : 'text transform' ;
fragment TEXT_TRANSFORM_UPPERCASE : 'uppercase' ;
PROPERTY_TEXT_TRANSFORM_VALUE : NONE | TEXT_TRANSFORM_UPPERCASE ;

PROPERTY_TEXT_ALIGN_KEY : 'text align' ;
fragment TEXT_ALIGN_LEFT : 'left' ;
fragment TEXT_ALIGN_CENTER : 'center' ;
fragment TEXT_ALIGN_RIGHT : 'right' ;
PROPERTY_TEXT_ALIGN_VALUE : TEXT_ALIGN_LEFT | TEXT_ALIGN_CENTER | TEXT_ALIGN_RIGHT ;

PROPERTY_TEXT_COLOR_KEY : 'text color' ;
PROPERTY_FONT_SIZE_KEY : 'font size' ;
PROPERTY_TEXT_KEY : 'value' ;
PROPERTY_TEXT_VALUE : '"' .+? '"' ;

COMPONENT_MUTATION : 'component mutation' ;
SHAPE_MUTATION : 'shape mutation' ;

SHAPE : 'shape' ;
SHAPE_RECTANGLE : 'rectangle' ;
SHAPE_CIRCLE : 'circle' ;
SHAPE_TEXT : 'text' ;
SHAPE_PROPERTIES : 'properties' ;

POSITION : 'position' ;
POSITION_X : 'x' ;
POSITION_Y : 'y' ;
POSITION_SORTING : 'sorting' ;

VIEW : 'view' ;
ROOT : 'root' ;
REFERENCES : 'references' ;
COMPONENT : 'component' ;

CODE_MODULES : 'code modules' ;
BOOTS_SPECIFICATION : 'boots specification' ;
UMBRELLA_SPECIFICATION : 'umbrella specification' ;
CODE : '`' .+? '`';

SERVER : 'server' ;
ROUTE : 'route' ;
ROUTE_GET : 'GET' ;
ROUTE_POST : 'POST' ;
ROUTE_PUT : 'PUT' ;
ROUTE_DELETE : 'DELETE' ;
ID : [A-Za-z0-9]+ ;
