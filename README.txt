1. General Description
This Plugin for Atlassian FishEye (www.atlassian.com/fisheye) allows to automatically synchronize all projects
available in a Gerrit Code Review (code.google.com/p/gerrit/) to FishEye repositories. This makes it easier to use
FishEye as a repository browser for a whole Gerrit Code Review installation.

2. Installation
To install, please drop the plugin jar file into WEB-INF/lib and restart FishEye.

3. Configuration
Once the plugin is installed, a new option appears in the Administration area: "Gerrit Code Review Sync".
On this page, enter the details used to contact your Gerrit Code Review server. Only the default private/public key
authentication mode is supported.
The configuration will be validated when you save it.

4. Keeping in sync
On the Gerrit Configuration page, you will find a link "Sync Gerrit Code Review projects ... now". Use this URL at any
time to trigger the synchronization. This does not require any specific credentials and can therefore easily be called
from a script to provide a regular sync.
The sync page will log any changes it has made and report any errors it has encountered.


