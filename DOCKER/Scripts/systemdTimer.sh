#!/bin/bash
set -e
if [[ $# = 0 ]]; then
  echo "Usage: [SCRIPTS-PATH] [DOCKER-CONTAINER-NAME]"
  exit 0;
fi
echo "Creating systemd unit file and timer in $HOME/.config/systemd/user/"
scriptsPath=$1
dockerContainerName=$2
systemdUserPath="$HOME/.config/systemd/user"
cat > $systemdUserPath/NetworkCloudDriveBackup.timer<< EOF
[Unit]
Description=Periodically backup NetworkCloudDrive docker files

[Timer]
OnCalendar=*-*-01 08:00
OnCalendar=*-*-28 08:00
Unit=NetworkCloudDriveBackup.service

[Install]
WantedBy=timers.target
EOF
cat > $systemdUserPath/NetworkCloudDriveBackup.service<< EOF
[Unit]
Description=Periodically backup NetworkCloudDrive docker files

[Service]
ExecStart=/usr/bin/sh $scriptsPath/backupFiles.sh $dockerContainerName

[Install]
WantedBy=multi-default.target
EOF
systemctl --user daemon-reload
systemctl --user enable NetworkCloudDriveBackup.timer
systemctl --user start --now NetworkCloudDriveBackup.timer
systemctl --user start --now NetworkCloudDriveBackup.service
systemctl --user list-timers -a
echo "Created systemd service"