/* 
gcc -I/opt/local/include/gphoto2 -c ptp_lib.c
gcc -L/opt/local/lib -dynamiclib -lgphoto2 ptp_lib.o -o libptp_lib.dylib
*/

#include <stdlib.h>
#include <gphoto2/gphoto2.h>

typedef struct{
  Camera *camera;
  GPContext *context;
} ptp_handle;

void* ptp_init(){
  ptp_handle* ptr = (ptp_handle*) malloc(sizeof(ptp_handle));

  ptr->context = gp_context_new();
  gp_camera_new(&ptr->camera);

  int retval = gp_camera_init(ptr->camera, ptr->context);
  if (retval != GP_OK)
    return NULL;

  return (void*) ptr;
}

CameraWidget* get_widget(CameraWidget* root, char* path[], int count){
  CameraWidget *config = root;
  CameraWidget *child;

  int i=0;
  for(i = 0 ; i < count; i++){
    gp_widget_get_child_by_name(config, path[i], &child);
    config = child;
  }

  return config;
}

void toggle_capture(Camera *camera, GPContext *context, int state){
  CameraWidget *root;
  gp_camera_get_config(camera, &root, context);
  char* path[3]={"main","settings","capture"};
  CameraWidget *capture = get_widget(root,path,3);

  gp_widget_set_value(capture, &state);
  gp_camera_set_config(camera, root, context);
}

int extend_lens(ptp_handle* handle){
  toggle_capture(handle->camera,handle->context,1);
  return 1;
}

int retract_lens(ptp_handle* handle){
  toggle_capture(handle->camera,handle->context,0);
  return 1;
}

int preview(ptp_handle* handle, char *fn){
  int fd, retval;
  CameraFile *file;

  retval = gp_file_new(&file);
  if (retval != GP_OK)
    return 0;

  retval = gp_camera_capture_preview(handle->camera, file, handle->context);
  if (retval != GP_OK)
    return 0;

  retval = gp_file_save(file, fn);
  if (retval != GP_OK)
    return 0;

  gp_file_unref(file);
  return 1;
}

int ptp_exit(ptp_handle* handle){
  gp_camera_exit(handle->camera, handle->context);
  return 1;
}
