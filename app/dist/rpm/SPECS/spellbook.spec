%define _topdir /home/bozhidar/spellbook-rpm

Summary: Spellbook is a platform independent dictionary written in Java 
Name: spellbook
Version: 0.3.0
Release: 1
BuildArch: noarch
License: GPL v3
Group: Development/Tools
URL: http://code.google.com/spellbook-dictionary
Source0: %{name}-%{version}.tar.bz2
BuildRoot: %{_topdir}/%{name}-%{version}-%{release}-root
Requires: jre >= 1.6

%description
Spellbook is a platform independent dictionary written in Java


%prep
%setup -q

%build
export LANG=en_US.UTF-8

mvn clean install
cd desktop/
mvn clean assembly:assembly

%install
rm -rf $RPM_BUILD_ROOT
mkdir -p $RPM_BUILD_ROOT/opt/spellbook
mkdir -p $RPM_BUILD_ROOT/usr/bin
mkdir -p $RPM_BUILD_ROOT/usr/share/icons/hicolor/128x128/apps/
mkdir -p $RPM_BUILD_ROOT/usr/share/icons/hicolor/48x48/apps/
mkdir -p $RPM_BUILD_ROOT/usr/share/icons/hicolor/32x32/apps/
mkdir -p $RPM_BUILD_ROOT/usr/local/share/applications
cp desktop/target/spellbook-ui-0.3.0-jar-with-dependencies.jar $RPM_BUILD_ROOT/opt/spellbook
cp app/icons/dictionary128.png $RPM_BUILD_ROOT/usr/share/icons/hicolor/128x128/apps/spellbook.png
cp app/icons/dictionary48.png $RPM_BUILD_ROOT/usr/share/icons/hicolor/48x48/apps/spellbook.png
cp app/icons/dictionary32.png $RPM_BUILD_ROOT/usr/share/icons/hicolor/32x32/apps/spellbook.png
cp app/bin/spellbook.desktop $RPM_BUILD_ROOT/usr/local/share/applications
cp app/bin/spellbook $RPM_BUILD_ROOT/usr/bin

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-,root,root,-)
/opt/spellbook/spellbook-ui-0.3.0-jar-with-dependencies.jar
/usr/share/icons/hicolor/128x128/apps/spellbook.png
/usr/share/icons/hicolor/48x48/apps/spellbook.png
/usr/share/icons/hicolor/32x32/apps/spellbook.png
/usr/local/share/applications/spellbook.desktop

%attr(755,root,root)
/usr/bin/spellbook

%doc

%changelog
* Wed May 12 2010 Bozhidar Batsov <bozhidar@bozhidar-desktop> - 0.3.0-1
- Spellbook 0.3.0
