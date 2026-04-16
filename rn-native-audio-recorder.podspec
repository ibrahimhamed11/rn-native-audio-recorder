require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name         = package['name']
  s.version      = package['version']
  s.summary      = package['description']
  s.homepage     = package['homepage']
  s.license      = package['license']
  s.author       = package['author']['name']
  s.source       = { :git => package['repository']['url'], :tag => s.version }

  s.ios.deployment_target = '13.0'

  s.source_files = 'ios/**/*.{swift,m}'

  s.dependency 'React-Core'
end
