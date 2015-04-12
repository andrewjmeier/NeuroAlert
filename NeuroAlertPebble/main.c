nclude <pebble.h>
  
static Window *s_main_window;

// This is a scroll layer
// static ScrollLayer *s_scroll_layer;

// We also use a text layer to scroll in the scroll layer
static TextLayer *s_text_layer;

// The scroll layer can other things in it such as an invert layer
static InverterLayer *s_inverter_layer;
// static char s_scroll_text[] = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam quam tellus, fermentu  m quis vulputate quis, vestibulum interdum sapien. Vestibulum lobortis pellentesque pretium. Quisque ultricies purus e  u orci convallis lacinia. Cras a urna mi. Donec convallis ante id dui dapibus nec ullamcorper erat egestas. Aenean a m  auris a sapien commodo lacinia. Sed posuere mi vel risus congue ornare. Curabitur leo nisi, euismod ut pellentesque se  d, suscipit sit amet lorem. Aliquam eget sem vitae sem aliquam ornare. In sem sapien, imperdiet eget pharetra a, lacin  ia ac justo. Suspendisse at ante nec felis facilisis eleifend.";
static const uint32_t segments[] = { 250, 70, 250, 70, 250, 450, 250, 70, 250, 70, 250};
static Layer *window_layer;

bool canTest = true;

void send_int(uint8_t key, uint8_t cmd)
{
    DictionaryIterator *iter;
    app_message_outbox_begin(&iter);
      
    Tuplet value = TupletInteger(key, cmd);
    dict_write_tuplet(iter, &value);
      
    app_message_outbox_send();
}

void sendTest(void* data){
  send_int(100, 100);
}

void testConnection(){
  if (!canTest) {
    return;
  }
  char* str = "Testing Connection...";
  text_layer_set_text(s_text_layer, str);
//   Layer *window_layer = window_get_root_layer(s_main_window);
//   GRect bounds = layer_get_frame(window_layer);

  // Trim text layer and scroll content to fit text box
  GSize max_size = text_layer_get_content_size(s_text_layer);
  text_layer_set_size(s_text_layer, GSize(max_size.w, max_size.h + 4));
//   scroll_layer_set_content_size(s_scroll_layer, GSize(bounds.size.w, max_size.h));
  sendTest(NULL);
}

static void select_click_handler(ClickRecognizerRef recognizer, void *context) {
  testConnection();
  APP_LOG(APP_LOG_LEVEL_DEBUG, "button clicked");

}

static void back_click_handler(ClickRecognizerRef recognizer, void *context) {
  if (canTest) {
    window_stack_pop(true);
  } else {
    canTest = true;
    testConnection();
  }
}

static void click_config_provider(void *context) {
  window_single_click_subscribe(BUTTON_ID_SELECT, select_click_handler);
  window_single_click_subscribe(BUTTON_ID_BACK, back_click_handler);
    APP_LOG(APP_LOG_LEVEL_DEBUG, "provider done");
}

static void window_load(Window *window) {
  window_layer = window_get_root_layer(window);
  GRect bounds = layer_get_frame(window_layer);
  GRect max_text_bounds = GRect(0, 0, bounds.size.w, 2000);

  // Initialize the scroll layer
//   s_scroll_layer = scroll_layer_create(bounds);

  // This binds the scroll layer to the window so that up and down map to scrolling
  // You may use scroll_layer_set_callbacks to add or override interactivity
//   scroll_layer_set_click_config_onto_window(s_scroll_layer, window);

  // Initialize the text layer
  s_text_layer = text_layer_create(max_text_bounds);
  text_layer_set_text(s_text_layer, "");

  // Change the font to a nice readable one
  // This is system font; you can inspect pebble_fonts.h for all system fonts
  // or you can take a look at feature_custom_font to add your own font
  text_layer_set_font(s_text_layer, fonts_get_system_font(FONT_KEY_GOTHIC_28));

  // Trim text layer and scroll content to fit text box
  GSize max_size = text_layer_get_content_size(s_text_layer);
  text_layer_set_size(s_text_layer, max_size);
//   scroll_layer_set_content_size(s_scroll_layer, GSize(bounds.size.w, max_size.h + 4));

  
  layer_add_child(window_layer, text_layer_get_layer(s_text_layer));
  // Add the layers for display
//   scroll_layer_add_child(s_scroll_layer, text_layer_get_layer(s_text_layer));

//   layer_add_child(window_layer, scroll_layer_get_layer(s_scroll_layer));
  
//   scroll_layer_set_context(s_scroll_layer, s_text_layer);
  
//   ScrollLayerCallbacks cbacks;

//   cbacks.click_config_provider = &click_config_provider;
//   scroll_layer_set_callbacks(s_scroll_layer, cbacks);
  
  
}
 
static void main_window_unload(Window *window) {
  inverter_layer_destroy(s_inverter_layer);
  text_layer_destroy(s_text_layer);
//   scroll_layer_destroy(s_scroll_layer);
}
 
static void timerCallbackInverse(void* data) {
  // The inverter layer will highlight some text
  GSize max_size = text_layer_get_content_size(s_text_layer);
  s_inverter_layer = inverter_layer_create(GRect(0, 0, max_size.w + 4, max_size.h));
  layer_add_child(window_layer, inverter_layer_get_layer(s_inverter_layer));
//   scroll_layer_add_child(s_scroll_layer, inverter_layer_get_layer(s_inverter_layer));
}

static void timerCallbackRegular(void* data) {
  layer_remove_from_parent((Layer*)s_inverter_layer);
}
static void out_failed_handler(DictionaryIterator *iter, AppMessageResult reason, void *context) {
  char* str = "Connection to Android App Failed \nPress Select to Test Again";
  text_layer_set_text(s_text_layer, str);
//   Layer *window_layer = window_get_root_layer(s_main_window);
//   GRect bounds = layer_get_frame(window_layer);

  // Trim text layer and scroll content to fit text box
  GSize max_size = text_layer_get_content_size(s_text_layer);
  text_layer_set_size(s_text_layer, GSize(max_size.w, max_size.h + 4));
//   scroll_layer_set_content_size(s_scroll_layer, GSize(bounds.size.w, max_size.h));
}

static void out_sent_handler(DictionaryIterator *iter, void *context) {
  char* str = "Connected to Android App";
  text_layer_set_text(s_text_layer, str);
//   Layer *window_layer = window_get_root_layer(s_main_window);
//   GRect bounds = layer_get_frame(window_layer);

  // Trim text layer and scroll content to fit text box
  GSize max_size = text_layer_get_content_size(s_text_layer);
  text_layer_set_size(s_text_layer, GSize(max_size.w, max_size.h + 4));
//   scroll_layer_set_content_size(s_scroll_layer, GSize(bounds.size.w, max_size.h));
}


static void in_received_handler(DictionaryIterator *iter, void *context) 
{
  // Vibe pattern: ON for 200ms, OFF for 100ms, ON for 400ms:

VibePattern pat = {
  .durations = segments,
  .num_segments = ARRAY_LENGTH(segments),
};

  
  Tuple *t = dict_read_first(iter);
    if(t)
    {
        vibes_enqueue_custom_pattern(pat);
        //vibes_short_pulse();
        char* str = (char*)t->value->cstring;
        text_layer_set_text(s_text_layer, str);
//         Layer *window_layer = window_get_root_layer(s_main_window);
//         GRect bounds = layer_get_frame(window_layer);

        // Trim text layer and scroll content to fit text box
        GSize max_size = text_layer_get_content_size(s_text_layer);
        text_layer_set_size(s_text_layer, GSize(max_size.w, max_size.h + 4));
//         scroll_layer_set_content_size(s_scroll_layer, GSize(bounds.size.w, max_size.h));
        canTest = false;

        app_timer_register(0, timerCallbackInverse, NULL);
        app_timer_register(500, timerCallbackRegular, NULL);
        app_timer_register(1000, timerCallbackInverse, NULL);
        app_timer_register(1500, timerCallbackRegular, NULL);
        app_timer_register(2000, timerCallbackInverse, NULL);
        app_timer_register(2500, timerCallbackRegular, NULL);
      
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
  
  testConnection();
  
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
