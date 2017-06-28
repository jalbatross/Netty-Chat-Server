@0x9f92c32bbbfa1550;

#How to compile this file:
#1. Make sure that java.capnp is in the same directory, or the directory 
#   indicated after the import phrase
#2. Put capnpc-java in the same directory as this file
#3. Using terminal, navigate to the directory of this file and issue this command:
#   capnpc compile -o./capnpc-java credential.capnp  

using Java = import "java.capnp";
$Java.package("com.test.chatserver");
$Java.outerClassname("Credential");


struct Credential {
  username @0 :Text;
  password @1 :Text;
}

