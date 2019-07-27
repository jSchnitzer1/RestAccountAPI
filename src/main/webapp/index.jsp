<!DOCTYPE html>
<html>
<head>
    <title>My Pages</title>
    <script type="text/javascript" src="https://code.jquery.com/jquery-3.4.1.min.js"></script>

</head>
<body>
    <p><a id="checkAccountEndpoint" href="">Test Account API</a>
    <script>
        (function ($) {
            function processForm() {
                $.ajax({
                    url: 'api/account/checkAccountEndpoint',
                    dataType: 'text',
                    type: 'post',
                    data: '',
                    success: function (data, textStatus, jQxhr) {
                        alert(data);
                    },
                    error: function (jqXhr, textStatus, errorThrown) {
                        alert(errorThrown);
                    }
                });
            }

            $('#checkAccountEndpoint').click(function (e) {
                e.preventDefault();
                processForm();
            });
        })(jQuery);
    </script>
</body>
</html>
