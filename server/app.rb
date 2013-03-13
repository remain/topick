require 'open-uri'
require 'net/http'
require 'rexml/document'

require File.join(DIR, 'lib', 'Hash')
require File.join(DIR, 'lib', 'create_url')


class Topick < Sinatra::Base
	configure :development do
		Bundler.require :development
		register Sinatra::Reloader

		require 'pp'
	end

	configure do
		enable :sessions

		set(:facebook) { YAML.load_file(FACEBOOK_CONF).symbolize_keys }
		set(:yahoo_api) { YAML.load_file(YAHOO_API_CONF).symbolize_keys }
	end
	
	def oauth_facebook
		Koala::Facebook::OAuth.new(
			settings.facebook[:app_id],
			settings.facebook[:app_secret],
			create_url(request.scheme, request.host, '/auth/facebook/callback'))
	end


	get '/search/facebook' do
		halt 400 if session[:access_token_facebook].blank?
		halt 400 if params[:name].blank?

		q = Array.new
		if params[:name] =~ /[^ -~｡-ﾟ]/ then
			q << params[:name].gsub(/　/, "\s").to_roman
			q << String.new
			q.first.split(/\s/).each do |str|
				q.last << "#{str.to_kunrei} "
			end
			q.last.chop!
		else
			q << params[:name].gsub(/　/, "\s")
		end

		results = Array.new
		graph = Koala::Facebook::API.new(session[:access_token_facebook])
		begin
			q.each do |name|
				graph.graph_call('search', :type => 'user', :q => name).each do |user|
					results << user['id']
				end
			end

			pp graph.get_objects(results.uniq, :locale => 'ja_JP')
		rescue Koala::KoalaError
			halt 400
		end

		200
	end

	get '/keyphrase/facebook' do
		halt 400 if session[:access_token_facebook].blank?

		posts = Array.new
		graph = Koala::Facebook::API.new(session[:access_token_facebook])
		begin
			graph.get_connections('me', 'feed', :limit => 50).each do |feed|
				posts << feed['message'] unless feed['message'].nil?
				if feed['type'] == 'link' and feed['status_type'] == 'shared_story' then
					posts << feed['name'] unless feed['name'].nil?
					posts << feed['description'] unless feed['description'].nil?
				end
			end
		rescue Koala::KoalaError
			halt 400
		end
		
		posts.each do |post|
			next unless post =~ /[^ -~｡-ﾟ]/
			Net::HTTP.start(settings.yahoo_api[:host]) do |http|
				response = http.post(settings.yahoo_api[:path], "appid=#{settings.yahoo_api[:app_id]}&sentence=#{URI.encode(post)}")
				REXML::Document.new(response.body).elements.each('ResultSet/Result') do |result|
					puts "#{result.elements['Score'].text.rjust(3, '0')}: #{result.elements['Keyphrase'].text}"
				end
			end
		end

		200
	end

	get '/auth/facebook' do
		halt 400 if session[:access_token_facebook].blank?
		"<script>Login.sendAccessToken(\"#{session[:access_token_facebook]}\");</script>"
	end

	get '/auth/facebook/login' do
		session[:access_token_facebook] = nil
		redirect oauth_facebook.url_for_oauth_code(:scope => 'read_stream')
	end

	get '/auth/facebook/callback' do
		halt 400 if params[:code].blank?
		session[:access_token_facebook] = oauth_facebook.get_access_token(params[:code])
		redirect '/search/facebook?name=tanabe%20yoichi'
	end
end
