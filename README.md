# NextcloudUserImporter

A Java program to add users to Nextcloud from a CSV.

BUILD USING: ./gradlew build

EXECUTE USING: sudo -u www-data java -jar build/libs/NextcloudUserImporter.jar &lt;member_file&gt;.csv &lt;path to Nextcloud OCC executable&gt;

For example: sudo -u www-data java -jar build/libs/NextcloudUserImporter.jar ~/members.csv /var/www/cloud.ucubesat.org/occ

Member file should be a CSV formatted as follows: first_name,last_name,UID_email

The default password for all added users will be: &lt;username&gt;123

The default group for all added users will be: Member