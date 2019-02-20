
var webpack = require('webpack');
var CopyWebpackPlugin = require('copy-webpack-plugin');

var buildDirRegex = /(node_modules|web_modules|bower_components|libs)/;

var devMode = false;
process.argv.forEach(function(val, index) {
  if(val == '-d') devMode = true;
});

module.exports = {
    devtool: devMode ? 'source-map' : 'cheap-module-source-map',
    entry: {
		index: "./js/index.js",
		app: "./js/app.js",
		quick: "./js/quick.js",
		camera: "./js/camera.js"
	}, output: {
        path: __dirname+"/../../../build-webapp",
        filename: "js/[name].js"
    }, module: {
        rules: [
			{ test: /\.js$/, enforce: 'pre', exclude: buildDirRegex, use: ['ng-annotate-loader', 'source-map-loader','babel-loader?presets=es2015', 'jshint-loader']},
            { test: /\.css$/, use: ["style-loader", {loader: "css-loader", options: {minimize: true}}] },
			{ test: /\.scss$/, use: ["style-loader", {loader: "css-loader", options: {minimize: true}}, "sass-loader"] },
			{ test: /\.coffee$/, use: "coffee-loader" },
			{ test: /\.(svg|ttf|woff|eot|png|gif|jpg|jpeg)/, use: "file-loader?name=[name].[ext]" }
        ]
    }, resolve: {
		modules: ["web_modules", "node_modules", "bower_components"]
    }, plugins: [
		new CopyWebpackPlugin([
			{ from: 'favicon.ico' },
			{ from: 'images', to: 'images' },
			{ from: 'partials', to: 'partials' },
			{ from: 'html' }
		])
    ]
};