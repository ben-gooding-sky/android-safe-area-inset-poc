## Set Android Only CSS PoC

This Poc attempts to apply some css only on android webviews only

### Conclusion

For a normal webpage this is possible using CSS `vars`

It is done by injecting this css var in the android webview with 
```javaScript
    document.documentElement.style.setProperty('--isAndroid', 1)
```

and set the CSS with
```css
    @container style(--isAndroid: 1) {
        .bottom-text {
                margin-bottom: 50px;
        }
    }
```

#### Iframe Limitation
If the content you want to alter is within an Iframe, it isn't possible to alter as CORS will block
any attempt at altering contents from within an Iframe