#include <pebble.h>
  
static Window *s_main_window;

static TextLayer *s_text_layer;

static InverterLayer *s_inverter_layer;
static const uint32_t segments[] = { 250, 70, 250, 70, 250, 450, 250, 70, 250, 70, 250};
static Layer *window_layer;
int gameCount;
bool isPlaying;

char* itoa(int i, char b[]){
    char const digit[] = "0123456789";
    char* p = b;
    if(i<0){
        *p++ = '-';
        i *= -1;
    }
    int shifter = i;
    do{ //Move to where representation ends
        ++p;
        shifter = shifter/10;
    }while(shifter);
    *p = '\0';
    do{ //Move back, inserting digits as u go
        *--p = digit[i%10];
        i = i/10;
    }while(i);
    return b;
}

void send_int(uint8_t key, int cmd)
{
    DictionaryIterator *iter;
    app_message_outbox_begin(&iter);
      
    Tuplet value = TupletInteger(key, cmd);
    dict_write_tuplet(iter, &value);
      
    app_message_outbox_send();
}


void startGame(){
  char* str = "Starting Game...";
  text_layer_set_text(s_text_layer, str);

  // Trim text layer and scroll content to fit text box
  GRect bounds = layer_get_frame(window_layer);
  GSize max_size = text_layer_get_content_size(s_text_layer);
  text_layer_set_size(s_text_layer, GSize(bounds.size.w, max_size.h + 4));
  send_int(23, 1);

}

static void select_click_handler(ClickRecognizerRef recognizer, void *context) {
  if (isPlaying) {
    return;
  }
  text_layer_set_font(s_text_layer, fonts_get_system_font(FONT_KEY_GOTHIC_28));
  startGame();
  APP_LOG(APP_LOG_LEVEL_DEBUG, "button clicked");

}

// static void back_click_handler(ClickRecognizerRef recognizer, void *context) {

// }

static void click_config_provider(void *context) {
  window_single_click_subscribe(BUTTON_ID_SELECT, select_click_handler);
//   window_single_click_subscribe(BUTTON_ID_BACK, back_click_handler);
    APP_LOG(APP_LOG_LEVEL_DEBUG, "provider done");
}

static void window_load(Window *window) {
  window_layer = window_get_root_layer(window);
  GRect bounds = layer_get_frame(window_layer);
  GRect max_text_bounds = GRect(0, 0, bounds.size.w, bounds.size.h);

  // Initialize the text layer
  s_text_layer = text_layer_create(max_text_bounds);
  text_layer_set_text(s_text_layer, "Press Select to Start");

  // Change the font to a nice readable one
  // This is system font; you can inspect pebble_fonts.h for all system fonts
  // or you can take a look at feature_custom_font to add your own font
  text_layer_set_font(s_text_layer, fonts_get_system_font(FONT_KEY_GOTHIC_28));

  text_layer_set_text_alignment(s_text_layer, GTextAlignmentCenter);
  // Trim text layer and scroll content to fit text box
  GSize max_size = text_layer_get_content_size(s_text_layer);
  text_layer_set_size(s_text_layer, GSize(bounds.size.w - 4, max_size.h + 4));

  
  layer_add_child(window_layer, text_layer_get_layer(s_text_layer)); 
}

void gameWon(){
  text_layer_set_text(s_text_layer, "You Win!");
  
  GRect bounds = layer_get_frame(window_layer);
  GSize max_size = text_layer_get_content_size(s_text_layer);
  text_layer_set_size(s_text_layer, GSize(bounds.size.w, max_size.h + 4));
  
  send_int(46, gameCount);
}

void gameLost(){
  text_layer_set_text(s_text_layer, "You Lost");
  
  GRect bounds = layer_get_frame(window_layer);
  GSize max_size = text_layer_get_content_size(s_text_layer);
  text_layer_set_size(s_text_layer, GSize(bounds.size.w, max_size.h + 4));
  
    send_int(46, gameCount);
}
 
static void main_window_unload(Window *window) {
  inverter_layer_destroy(s_inverter_layer);
  text_layer_destroy(s_text_layer);
}
 #define GAME_END_KEY 46


static void out_failed_handler(DictionaryIterator *iter, AppMessageResult reason, void *context) {
    Tuple *t = dict_read_first(iter);
  
  switch(t->key) {
    case GAME_END_KEY:
    return;
  }
  
  char* str = "Connection to Android App Failed";
  
  
  //TAKE THIS OUT!!!! ---------------------------------
  //TAKE THIS OUT!!!! ---------------------------------
  //TAKE THIS OUT!!!! ---------------------------------
  //TAKE THIS OUT!!!! ---------------------------------
  //TAKE THIS OUT!!!! ---------------------------------
  //TAKE THIS OUT!!!! ---------------------------------
  //TAKE THIS OUT!!!! ---------------------------------
  //TAKE THIS OUT!!!! ---------------------------------
  //TAKE THIS OUT!!!! ---------------------------------
  isPlaying = true;
  //TAKE THIS OUT!!!! ---------------------------------
  //TAKE THIS OUT!!!! ---------------------------------
  //TAKE THIS OUT!!!! ---------------------------------
  //TAKE THIS OUT!!!! ---------------------------------
  //TAKE THIS OUT!!!! ---------------------------------
  //TAKE THIS OUT!!!! ---------------------------------
  //TAKE THIS OUT!!!! ---------------------------------
  
  text_layer_set_text(s_text_layer, str);


  // Trim text layer and scroll content to fit text box
  GRect bounds = layer_get_frame(window_layer);
  GSize max_size = text_layer_get_content_size(s_text_layer);
  text_layer_set_size(s_text_layer, GSize(bounds.size.w, max_size.h + 4));
}

static void out_sent_handler(DictionaryIterator *iter, void *context) {
  Tuple *t = dict_read_first(iter);
  
  switch(t->key) {
    case GAME_END_KEY:
    return;
  }


  char* str = "STARE!!";
  isPlaying = true;
  text_layer_set_text(s_text_layer, str);

  // Trim text layer and scroll content to fit text box
  GRect bounds = layer_get_frame(window_layer);
  GSize max_size = text_layer_get_content_size(s_text_layer);
  text_layer_set_size(s_text_layer, GSize(bounds.size.w, max_size.h + 4));
}

#define BLINK_KEY 12
static void in_received_handler(DictionaryIterator *iter, void *context) 
{
  // Vibe pattern: ON for 200ms, OFF for 100ms, ON for 400ms:

VibePattern pat = {
  .durations = segments,
  .num_segments = ARRAY_LENGTH(segments),
};

  
  Tuple *t = dict_read_first(iter);
  
  // Process all pairs present
  while(t != NULL) {
    // Process this pair's key
    switch (t->key) {
      case BLINK_KEY:
        APP_LOG(APP_LOG_LEVEL_INFO, "KEY_DATA received with value %d", (int)t->value->int32);
        text_layer_set_font(s_text_layer, fonts_get_system_font(FONT_KEY_GOTHIC_28));

        vibes_enqueue_custom_pattern(pat);
        
        int result = (int)t->value->int32;
        if (result == 0) {
          gameLost();
        } else {
          gameWon();
        }
        isPlaying = false;
      gameCount = 0;
        break;
    }

    // Get next pair, if any
    t = dict_read_next(iter);
    
  }
 
}



void update_time(){
  int minutes = gameCount / 60;
  int seconds = gameCount % 60;
  
  char minuteString[sizeof("00")];
  char* min = itoa(minutes, minuteString);


  
  char secondString[sizeof("00")];
  char* sec = itoa(seconds, secondString);

  char* buffer = strcat(min, "m ");
  char* new = strcat(buffer, sec);
  char* final = strcat(new, "s");
  text_layer_set_font(s_text_layer, fonts_get_system_font(FONT_KEY_BITHAM_42_BOLD));
  text_layer_set_text(s_text_layer, final);
  GRect bounds = layer_get_frame(window_layer);
  GSize max_size = text_layer_get_content_size(s_text_layer);
  text_layer_set_size(s_text_layer, GSize(bounds.size.w, max_size.h + 4));
  
  gameCount++;

}

static void tick_handler(struct tm *tick_time, TimeUnits units_changed) {
  if (isPlaying) {
     update_time(); 
  }
}

static void init(void) {
  s_main_window = window_create();
  window_set_click_config_provider(s_main_window, click_config_provider);
  window_set_window_handlers(s_main_window, (WindowHandlers) {
    .load = window_load,
    .unload = main_window_unload,
  });
  
  
  app_message_register_inbox_received(in_received_handler);    
  app_message_register_outbox_failed(out_failed_handler);
  app_message_register_outbox_sent(out_sent_handler);
  app_message_open(512, 512);
  window_stack_push(s_main_window, true);

  gameCount = 0;
  isPlaying = false;
  tick_timer_service_subscribe(SECOND_UNIT, tick_handler);
    
}
 
static void deinit(void) {
  window_destroy(s_main_window);
}
 

enum {
    KEY_BUTTON_EVENT = 0,
    BUTTON_EVENT_UP = 1,
    BUTTON_EVENT_DOWN = 2,
    BUTTON_EVENT_SELECT = 3
};

int main(void) {
  init();
 
  APP_LOG(APP_LOG_LEVEL_DEBUG, "Done initializing, pushed window: %p", s_main_window);
 
  app_event_loop();
  deinit();
}
