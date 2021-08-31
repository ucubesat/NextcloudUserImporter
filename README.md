A Java program to add users to Nextcloud from a CSV.

BUILD USING: ./gradlew build
EXECUTE USING: sudo -u www-data java -jar build/libs/NextcloudUserImporter.jar <member_file>.csv <path to Nextcloud OCC executable>
For example: sudo -u www-data java -jar build/libs/NextcloudUserImporter.jar ~/members.csv /var/www/cloud.ucubesat.org/occ

Member file should be a CSV formatted as follows: first_name,last_name,UID_email

The default password for all added users will be: <username>123
The default group for all added users will be: Member