echo "The webcam runs on port 8090.  To see it from your localhost, open http://localhost:8090 in your web browser.  Click refresh to get new images."
java -classpath lti-civil-no_s_w_t.jar -Djava.library.path="native/win32-x86" com.lti.civil.webcam.CivilJPEGServer