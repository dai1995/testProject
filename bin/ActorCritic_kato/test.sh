#!/bin/sh
java -jar su.jar ../para/parameter-ActorCritic-10-su.xml
java -jar su.jar ../para/parameter-ActorCritic-20-su.xml
java -jar nt.jar ../para/parameter-ActorCritic-5-nt.xml
java -jar nt.jar ../para/parameter-ActorCritic-10-nt.xml
java -jar nt.jar ../para/parameter-ActorCritic-20-su.xml
java -jar st.jar ../para/parameter-ActorCritic-5-st.xml
java -jar st.jar ../para/parameter-ActorCritic-10-st.xml
java -jar st.jar ../para/parameter-ActorCritic-20-st.xml

