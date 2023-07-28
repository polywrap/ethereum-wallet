Pod::Spec.new do |s|
    s.name             = 'MetamaskProviderPlugin'
    s.version          = '0.0.3'
    s.summary          = 'Metamask ethereum wallet plugin'
    s.homepage         = 'https://github.com/polywrap/ethereum-wallet'
    s.license          = 'MIT'
    s.author           = { 'Cesar' => 'cesar@polywrap.io' }
  
    s.source_files = 'implementations/swift/metamask/Source/**/*.swift'
    s.swift_version  = "5.0"
    s.ios.deployment_target = '14.0'
    s.user_target_xcconfig = { 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'x86_64' }
    s.pod_target_xcconfig = { 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'x86_64' }
    s.source = { :git => "https://github.com/polywrap/ethereum-wallet.git", :branch => 'feat/swift-implementation' }
    s.static_framework = true
    s.dependency 'PolywrapClient', '~> 0.0.5'
    s.dependency 'metamask-ios-sdk', '~> 0.2.0'
  end