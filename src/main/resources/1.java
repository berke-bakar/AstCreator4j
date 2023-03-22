// Taken from dataset at this link: https://www.google.com/url?q=https%3A%2F%2Fzenodo.org%2Frecord%2F7482720%2Ffiles%2Fdatasets_source.zip%3Fdownload%3D1&sa=D&sntz=1&usg=AOvVaw19ju_OaGpx_ewOUUr3RWl8
@java.lang.Override
public void onShowView() {
    super.onShowView();
    com.google.gwt.user.client.Window.enableScrolling(false);
    com.google.gerrit.client.JumpKeys.enable(false);
    if (prefs.hideTopMenu()) {
        com.google.gerrit.client.Gerrit.setHeaderVisible(false);
    }
    resizeHandler = com.google.gwt.user.client.Window.addResizeHandler(new com.google.gwt.event.logical.shared.ResizeHandler() {
        @java.lang.Override
        public void onResize(com.google.gwt.event.logical.shared.ResizeEvent event) {
            resizeCodeMirror();
        }
    });
}
